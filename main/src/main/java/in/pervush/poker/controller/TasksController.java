package in.pervush.poker.controller;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TaskUrlExistsException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.CreateTaskRequest;
import in.pervush.poker.model.tasks.DeleteTasksRequest;
import in.pervush.poker.model.tasks.TaskView;
import in.pervush.poker.model.tasks.TeamTasksStatView;
import in.pervush.poker.model.tasks.UserVotesStatView;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.service.TasksService;
import in.pervush.poker.service.UserService;
import in.pervush.poker.service.VotesService;
import in.pervush.poker.utils.InstantUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/teams/{teamUuid}/tasks")
@Tag(name="Team tasks")
@Validated
@SecurityRequirement(name = "Authorization")
public class TasksController {

    private final TasksService tasksService;
    private final VotesService votesService;
    private final UserService userService;

    public TasksController(TasksService tasksService, VotesService votesService, UserService userService) {
        this.tasksService = tasksService;
        this.votesService = votesService;
        this.userService = userService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get team tasks",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content(), description = "Team not found or you are not team member")
            }
    )
    public Collection<TaskView> getTasks(@PathVariable("teamUuid") final UUID teamUuid,
                                         @RequestParam(name = "search", required = false) String search,
                                         @RequestParam(name = "finished", required = false) Boolean finished,
                                         @AuthenticationPrincipal final UserDetailsImpl user) {
        return tasksService.getTasks(user.userUuid(), teamUuid, search, finished).stream()
                .map(v -> {
                    final var votes = votesService.getVotedUserUuids(v.taskUuid(), user.userUuid(), teamUuid);

                    return TaskView.of(
                            v,
                            userService.getUser(v.userUuid()),
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
                            @PathVariable("taskUuid") final UUID taskUuid,
                            @AuthenticationPrincipal final UserDetailsImpl user) {
        final var userUuid = user.userUuid();
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
                               @RequestBody @Valid final CreateTaskRequest request,
                               @AuthenticationPrincipal final UserDetailsImpl user) {
        try {
            return TaskView.of(
                    tasksService.createTask(
                            user.userUuid(),
                            request.name(),
                            request.url(),
                            request.scale(),
                            teamUuid
                    ),
                    userService.getUser(user.userUuid()),
                    Collections.emptyList()
            );
        } catch (TaskUrlExistsException ex) {
            throw new ErrorStatusException(ErrorStatus.TASK_URL_EXISTS);
        }
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
                           @PathVariable("taskUuid") final UUID taskUuid,
                           @AuthenticationPrincipal final UserDetailsImpl user) {
        tasksService.finishTask(taskUuid, user.userUuid(), teamUuid);
    }

    @Operation(
            summary = "Activate task",
            description = "Reset task votes and restart voting",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @PostMapping(value = "/{taskUuid}/activate")
    @ResponseStatus(HttpStatus.CREATED)
    public void activateTask(@PathVariable("teamUuid") final UUID teamUuid,
                             @PathVariable("taskUuid") final UUID taskUuid,
                             @AuthenticationPrincipal final UserDetailsImpl user) {
        tasksService.activateTask(taskUuid, user.userUuid(), teamUuid);
    }

    @Deprecated
    @Operation(
            summary = "Delete task",
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            },
            deprecated = true
    )
    @DeleteMapping(value = "/{taskUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable("teamUuid") final UUID teamUuid,
                           @PathVariable("taskUuid") final UUID taskUuid,
                           @AuthenticationPrincipal final UserDetailsImpl user) {
        tasksService.deleteTasks(Set.of(taskUuid), user.userUuid(), teamUuid);
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
    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTasks(@PathVariable("teamUuid") final UUID teamUuid,
                            @RequestBody @Valid final DeleteTasksRequest request,
                            @AuthenticationPrincipal final UserDetailsImpl user) {
        tasksService.deleteTasks(request.taskUuids(), user.userUuid(), teamUuid);
    }

    @GetMapping(path = "/votesStat", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get team votes stat",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    public TeamTasksStatView getTasksVotesStat(
            @PathVariable("teamUuid") final UUID teamUuid,
            @Schema(description = "Default is now() - 30 days")
            @RequestParam(name = "startTm", required = false) final Long startTm,
            @Schema(description = "Default is now()")
            @RequestParam(name = "endTm", required = false) final Long endTm,
            @AuthenticationPrincipal final UserDetailsImpl user
    ) {
        final var startDtm = getOrDefault(startTm, InstantUtils.now().minus(Duration.of(30, ChronoUnit.DAYS)));
        final var endDtm = getOrDefault(endTm, InstantUtils.now());
        final var userUuid = user.userUuid();

        final var usersVotesStat = votesService
                .getVotesStat(teamUuid, userUuid, startDtm, endDtm).stream()
                .map(v -> UserVotesStatView.of(userService.getUser(v.userUuid()), v)).toList();
        final int totalTasksCount = tasksService.getFinishedTasksCount(teamUuid, userUuid, startDtm, endDtm);

        return new TeamTasksStatView(totalTasksCount, usersVotesStat);
    }

    private static Instant getOrDefault(final Long tm, final Instant defaultDtm) {
        if (tm == null) {
            return defaultDtm;
        } else {
            return Instant.ofEpochSecond(tm);
        }
    }
}
