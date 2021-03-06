package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.TasksRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TasksService {

    public static final int TASK_NAME_NAME_MAX_LENGTH = 100;
    public static final int TASK_URL_NAME_MAX_LENGTH = 100;
    private static final UrlValidator URL_VALIDATOR = new UrlValidator(new String[] {"http", "https"});

    private final TasksRepository tasksRepository;

    public List<DBTask> getTasks(final UUID userUuid) {
        return tasksRepository.getNotDeletedTasks(userUuid);
    }

    public DBTask getTask(final UUID taskUuid) {
        return tasksRepository.getNotDeletedTask(taskUuid);
    }

    public DBTask createTask(final UUID userUuid, final String name, final String url, final Scale scale) {
        validateTaskName(name);
        validateTaskUrl(url);
        final var taskUuid = UUID.randomUUID();
        return tasksRepository.createTask(userUuid, taskUuid, name, url, scale);
    }

    @Transactional
    public void finishTask(final UUID taskUuid, final UUID userUuid) {
        final var dbTask = tasksRepository.getNotDeletedTaskLock(taskUuid, userUuid);
        if (dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        tasksRepository.finishTask(taskUuid, userUuid);
    }

    public void deleteTask(final UUID taskUuid, final UUID userUuid) {
        tasksRepository.deleteTask(taskUuid, userUuid);
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
