package in.pervush.poker.repository;

import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.user.DBUser;
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

    public List<DBTask> getNotDeletedTasks(UUID userUuid) {
        return mapper.getNotDeletedTasks(userUuid);
    }

    public DBTask getNotDeletedTask(UUID taskUuid, final UUID requestingUserUuid) {
        return mapper.getNotDeletedTask(taskUuid, requestingUserUuid).orElseThrow(NotFoundException::new);
    }

    public DBTask getNotDeletedTaskLock(UUID taskUuid, UUID userUuid) {
        return mapper.getNotDeletedTaskLock(taskUuid, userUuid).orElseThrow(NotFoundException::new);
    }

    public void finishTask(UUID taskUuid, UUID userUuid) {
        final boolean updated = mapper.setFinished(taskUuid, userUuid);
        if (!updated) {
            throw new NotFoundException();
        }
    }

    public void deleteTask(UUID taskUuid, UUID userUuid) {
        final boolean updated = mapper.setDeleted(taskUuid, userUuid);
        if (!updated) {
            throw new NotFoundException();
        }
    }

    public DBTask createTask(final UUID userUuid, final UUID taskUuid, final String name, final String url,
                             final Scale scale) {
        final var now = InstantUtils.now();
        mapper.createTask(userUuid, taskUuid, name, url, scale, now);
        return new DBTask(taskUuid, userUuid, name, url, scale, false, now, null);
    }

}
