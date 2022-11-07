package in.pervush.poker.repository;

import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.postgres.TasksMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TasksRepository {

    private final TasksMapper mapper;

    public List<DBTask> getNotDeletedTasks(final UUID teamUuid) {
        return mapper.getNotDeletedTasks(teamUuid);
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
        final boolean updated = mapper.setFinished(taskUuid, teamUuid);
        if (!updated) {
            throw new TaskNotFoundException();
        }
    }

    public void deleteTask(final UUID taskUuid, final UUID teamUuid) throws TaskNotFoundException {
        final boolean updated = mapper.setDeleted(taskUuid, teamUuid);
        if (!updated) {
            throw new TaskNotFoundException();
        }
    }

    public DBTask createTask(final UUID userUuid, final String name, final String url,
                             final Scale scale, final UUID teamUuid) {
        final var now = InstantUtils.now();
        final var taskUuid = UUID.randomUUID();
        mapper.createTask(userUuid, taskUuid, name, url, scale, now, teamUuid);
        return new DBTask(taskUuid, userUuid, name, url, scale, false, now, null, teamUuid);
    }

}
