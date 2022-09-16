package in.pervush.poker.controller;

import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.tasks.CreateTaskRequest;
import in.pervush.poker.model.tasks.TasksView;
import in.pervush.poker.service.TasksService;
import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/tasks")
@Tag(name="Tasks")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Authorization")
public class TasksController {

    private final TasksService tasksService;
    private final RequestHelper requestHelper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get my tasks",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    public Collection<TasksView> getTasks() {
        return tasksService.getTasks(requestHelper.getAuthenticatedUserUuid()).stream().map(TasksView::of).toList();
    }

    @Operation(
            summary = "Get task",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/{taskUuid}")
    public TasksView getTask(@PathVariable("taskUuid") final UUID taskUuid) {
        requestHelper.getAuthenticatedUserUuid();
        final var dbTask = tasksService.getTask(taskUuid);
        return TasksView.of(dbTask);
    }

    @Operation(
            summary = "Create task",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TasksView createTask(@RequestBody @Valid final CreateTaskRequest request) {
        return TasksView.of(tasksService.createTask(
                requestHelper.getAuthenticatedUserUuid(),
                request.getName(),
                request.getUrl(),
                request.getScale())
        );
    }

    @Operation(
            summary = "Finish voting",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{taskUuid}/finish")
    @ResponseStatus(HttpStatus.CREATED)
    public void finishTask(@PathVariable("taskUuid") final UUID taskUuid) {
        tasksService.finishTask(taskUuid, requestHelper.getAuthenticatedUserUuid());
    }

    @Operation(
            summary = "Delete task",
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE, value = "/{taskUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("taskUuid") final UUID taskUuid) {
        tasksService.deleteTask(taskUuid, requestHelper.getAuthenticatedUserUuid());
    }
}
