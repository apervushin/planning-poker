package in.pervush.poker.service;

import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.tasks.Status;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({UsersRepository.class, TasksService.class, TasksRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import(TestPostgresConfiguration.class)
@Transactional
public class TasksServiceTests {

    private static final UUID USER_UUID = UUID.randomUUID();

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksService tasksService;

    @BeforeEach
    void initUser() {
        usersRepository.createUser(USER_UUID, "Test user");
    }

    @Test
    void createAndGetTask_success() {
        final var expected = tasksService.createTask(USER_UUID, "Test task",
                "http://localhost:1234/task?param=123#test", Scale.FIBONACCI);
        final var actual = tasksService.getTask(expected.taskUuid());
        assertEquals(expected, actual);
    }

    @Test
    void getTask_notFoundException() {
        assertThrows(NotFoundException.class, () -> tasksService.getTask(UUID.randomUUID()));
    }

    @Test
    void createAndGetTask_tooLongTaskName_errorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> tasksService.createTask(
                USER_UUID,
                RandomStringUtils.random(TasksService.TASK_NAME_NAME_MAX_LENGTH + 1),
                "http://localhost:1234/task?param=123#test",
                Scale.FIBONACCI
        ));
        assertEquals(ErrorStatus.INVALID_TASK_NAME, ex.getStatus());
    }

    @Test
    void createAndGetTask_tooLongUrl_errorStatusException() {
        final String validUrl = "http://localhost:1234/task?param=123";
        final var ex = assertThrows(ErrorStatusException.class, () -> tasksService.createTask(
                USER_UUID,
                "Test task",
                validUrl + RandomStringUtils.random(TasksService.TASK_NAME_NAME_MAX_LENGTH - validUrl.length() + 1),
                Scale.FIBONACCI
        ));
        assertEquals(ErrorStatus.INVALID_TASK_URL, ex.getStatus());
    }

    @Test
    void createAndFinishTask_success() {
        final var expected = tasksService.createTask(USER_UUID, "Test task",
                "http://localhost:1234/task?param=123#test", Scale.FIBONACCI);
        tasksService.finishTask(expected.taskUuid(), expected.userUuid());
        final var actual = tasksService.getTask(expected.taskUuid());
        assertEquals(Status.FINISHED, actual.status());
    }

    @Test
    void createAndDeleteTask_success() {
        final var expected = tasksService.createTask(USER_UUID, "Test task",
                "http://localhost:1234/task?param=123#test", Scale.FIBONACCI);
        tasksService.deleteTask(expected.taskUuid(), expected.userUuid());
        assertThrows(NotFoundException.class, () -> tasksService.getTask(expected.taskUuid()));
    }
}
