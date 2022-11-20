package in.pervush.poker.repository;

import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TaskUrlExistsException;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.postgres.TasksMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TasksRepository {

    private final TasksMapper mapper;

    public List<DBTask> getNotDeletedTasks(final UUID teamUuid, final UUID requestingUserUuid,
                                           @Nullable final String search, @Nullable final Boolean finished) {
        return mapper.getNotDeletedTasks(teamUuid, requestingUserUuid, search, finished);
    }

    public DBTask getNotDeletedTask(final UUID taskUuid, final UUID teamUuid, final UUID requestingUserUuid)
            throws TaskNotFoundException {
        return mapper.getNotDeletedTask(taskUuid, teamUuid, requestingUserUuid).orElseThrow(TaskNotFoundException::new);
    }

    public DBTask getNotDeletedTaskLock(final UUID taskUuid, final UUID teamUuid, final UUID userUuid)
            throws TaskNotFoundException {
        return mapper.getNotDeletedTaskLock(taskUuid, teamUuid, userUuid).orElseThrow(TaskNotFoundException::new);
    }

    public void finishTask(final UUID taskUuid, final UUID teamUuid) throws TaskNotFoundException {
        final boolean updated = mapper.setFinished(taskUuid, teamUuid, true);
        if (!updated) {
            throw new TaskNotFoundException();
        }
    }

    public void activateTask(final UUID taskUuid, final UUID teamUuid) throws TaskNotFoundException {
        final boolean updated = mapper.setFinished(taskUuid, teamUuid, false);
        if (!updated) {
            throw new TaskNotFoundException();
        }
    }

    public void deleteTasks(final Collection<UUID> taskUuids, final UUID teamUuid) throws TaskNotFoundException {
        if (taskUuids.isEmpty()) {
            return;
        }
        final boolean updated = mapper.setDeleted(taskUuids, teamUuid);
        if (!updated) {
            throw new TaskNotFoundException();
        }
    }

    public DBTask createTask(final UUID userUuid, final String name, final String url, final Scale scale,
                             final UUID teamUuid) throws TaskUrlExistsException {
        final var now = InstantUtils.now();
        final var taskUuid = UUID.randomUUID();
        try {
            mapper.createTask(userUuid, taskUuid, name, url, scale, now, teamUuid);
        } catch (DuplicateKeyException ex) {
            throw new TaskUrlExistsException();
        }
        return new DBTask(taskUuid, userUuid, name, url, scale, false, now, null, teamUuid);
    }

    public int getFinishedTasksCount(final UUID teamUuid, final Instant startDtm, final Instant endDtm) {
        return mapper.getFinishedTasksCount(teamUuid, startDtm, endDtm);
    }

}
