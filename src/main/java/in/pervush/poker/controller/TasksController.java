package in.pervush.poker.controller;

import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.tasks.CreateTaskRequest;
import in.pervush.poker.model.tasks.TaskView;
import in.pervush.poker.service.TasksService;
import in.pervush.poker.service.UserService;
import in.pervush.poker.service.VotesService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/teams/{teamUuid}/tasks")
@Tag(name="Team tasks")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Authorization")
public class TasksController {

    private final TasksService tasksService;
    private final VotesService votesService;
    private final UserService userService;
    private final RequestHelper requestHelper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get my tasks",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content(), description = "Team not found or you are not team member")
            }
    )
    public Collection<TaskView> getTasks(@PathVariable("teamUuid") final UUID teamUuid,
                                         @RequestParam(name = "search", required = false) String search,
                                         @RequestParam(name = "finished", required = false) Boolean finished) {
        final var user = requestHelper.getAuthenticatedUser();

        return tasksService.getTasks(user.userUuid(), teamUuid, search, finished).stream()
                .map(v -> {
                    final var votes = votesService.getVotedUserUuids(v.taskUuid(), user.userUuid(), teamUuid);

                    return TaskView.of(
                            v,
                            user,
                            votes.stream().map(userService::getUser).collect(Collectors.toList())
                    );
                }).toList();
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
    public TaskView getTask(@PathVariable("teamUuid") final UUID teamUuid,
                            @PathVariable("taskUuid") final UUID taskUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        final var dbTask = tasksService.getTask(taskUuid, userUuid, teamUuid);
        final var taskUser = userService.getUser(dbTask.userUuid());
        final var userUuids = votesService.getVotedUserUuids(taskUuid, userUuid, teamUuid);
        return TaskView.of(dbTask, taskUser,
                userUuids.stream().map(userService::getUser).collect(Collectors.toList()));
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
    public TaskView createTask(@PathVariable("teamUuid") final UUID teamUuid,
                               @RequestBody @Valid final CreateTaskRequest request) {
        final var user = requestHelper.getAuthenticatedUser();
        return TaskView.of(
                tasksService.createTask(
                        user.userUuid(),
                        request.getName(),
                        request.getUrl(),
                        request.getScale(),
                        teamUuid
                ),
                user,
                Collections.emptyList()
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
    @PostMapping(value = "/{taskUuid}/finish")
    @ResponseStatus(HttpStatus.CREATED)
    public void finishTask(@PathVariable("teamUuid") final UUID teamUuid,
                           @PathVariable("taskUuid") final UUID taskUuid) {
        tasksService.finishTask(taskUuid, requestHelper.getAuthenticatedUserUuid(), teamUuid);
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
    @DeleteMapping(value = "/{taskUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("teamUuid") final UUID teamUuid,
                           @PathVariable("taskUuid") final UUID taskUuid) {
        tasksService.deleteTask(taskUuid, requestHelper.getAuthenticatedUserUuid(), teamUuid);
    }
}
