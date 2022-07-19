package in.pervush.poker.repository;

import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.tasks.Status;
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
        return mapper.getTasks(userUuid, Status.DELETED);
    }

    public DBTask getNotDeletedTask(UUID taskUuid) {
        return mapper.getTask(taskUuid, Status.DELETED).orElseThrow(NotFoundException::new);
    }

    public DBTask getNotDeletedTaskLock(UUID taskUuid, UUID userUuid) {
        return mapper.getTaskLock(taskUuid, userUuid, Status.DELETED).orElseThrow(NotFoundException::new);
    }

    public void finishTask(UUID taskUuid, UUID userUuid) {
        mapper.setTaskStatus(taskUuid, userUuid, Status.FINISHED);
    }

    public void deleteTask(UUID taskUuid, UUID userUuid) {
        final boolean updated = mapper.setTaskStatus(taskUuid, userUuid, Status.DELETED);
        if (!updated) {
            throw new NotFoundException();
        }
    }

    public DBTask createTask(UUID userUuid, UUID taskUuid, String name, String url, Scale scale) {
        final var now = InstantUtils.now();
        final var status = Status.ACTIVE;
        mapper.createTask(userUuid, taskUuid, name, url, scale, status, now);
        return new DBTask(taskUuid, userUuid, name, url, scale, status, now, 0);
    }

}
