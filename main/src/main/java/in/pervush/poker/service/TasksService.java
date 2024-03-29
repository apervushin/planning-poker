package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TaskUrlExistsException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.events.TaskCreatedEvent;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.VotesRepository;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TasksService {

    public static final int TASK_NAME_NAME_MAX_LENGTH = 100;
    public static final int TASK_URL_NAME_MAX_LENGTH = 100;
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http", "https"});

    private final TasksRepository tasksRepository;
    private final TeamsService teamsService;
    private final VotesRepository votesRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TasksService(TasksRepository tasksRepository, TeamsService teamsService, VotesRepository votesRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.tasksRepository = tasksRepository;
        this.teamsService = teamsService;
        this.votesRepository = votesRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<DBTask> getTasks(final UUID userUuid, final UUID teamUuid, @Nullable final String search,
                                 @Nullable final Boolean finished)
            throws TeamNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        return tasksRepository.getNotDeletedTasks(teamUuid, userUuid, search, finished);
    }

    public DBTask getTask(final UUID taskUuid, final UUID requestingUserUuid, final UUID teamUuid)
            throws TaskNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, requestingUserUuid);
        return tasksRepository.getNotDeletedTask(taskUuid, teamUuid, requestingUserUuid);
    }

    public DBTask createTask(final UUID userUuid, final String name, final String url, final Scale scale,
                             final UUID teamUuid) throws TaskUrlExistsException {
        validateTaskName(name);
        validateTaskUrl(url);
        final var team = teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        final var task = tasksRepository.createTask(userUuid, name, url, scale, teamUuid);
        eventPublisher.publishEvent(new TaskCreatedEvent(
                task.userUuid(),
                task.taskUuid(),
                task.teamUuid(),
                team.teamName(),
                task.name(),
                tasksRepository.getUsersNotVotedTasksCount(task.teamUuid())
        ));
        return task;
    }

    @Transactional
    public void finishTask(final UUID taskUuid, final UUID userUuid, final UUID teamUuid) throws TaskNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        final var dbTask = tasksRepository.getNotDeletedTaskLock(taskUuid, teamUuid, userUuid);
        if (dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        tasksRepository.finishTask(taskUuid, teamUuid);
    }

    @Transactional
    public void activateTask(final UUID taskUuid, final UUID userUuid, final UUID teamUuid) throws TaskNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        final var dbTask = tasksRepository.getNotDeletedTaskLock(taskUuid, teamUuid, userUuid);
        if (!dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        votesRepository.eraseVotes(taskUuid);
        tasksRepository.activateTask(taskUuid, teamUuid);
    }

    public void deleteTasks(final Set<UUID> taskUuids, final UUID userUuid, final UUID teamUuid) throws TaskNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        tasksRepository.deleteTasks(taskUuids, teamUuid);
    }

    public int getFinishedTasksCount(final UUID teamUuid, final UUID userUuid, final Instant startDtm,
                                     final Instant endDtm) throws TeamNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        return tasksRepository.getFinishedTasksCount(teamUuid, startDtm, endDtm);
    }

    private static void validateTaskName(final String name) {
        if (Strings.isBlank(name) || name.length() > TASK_NAME_NAME_MAX_LENGTH) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_NAME);
        }
    }

    private static void validateTaskUrl(final String url) {
        if (url == null) {
            return;
        }

        if (Strings.isBlank(url) || url.length() > TASK_URL_NAME_MAX_LENGTH || !URL_VALIDATOR.isValid(url)) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_URL);
        }
    }
}
