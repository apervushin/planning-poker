package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.TeamsRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig({UsersRepository.class, TasksService.class, TasksRepository.class, TeamsRepository.class,
        TeamsService.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@Transactional
public class TasksServiceTests {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksService tasksService;

    @Autowired
    private TeamsRepository teamsRepository;

    private UUID userUuid;
    private UUID teamUuid;

    @BeforeEach
    void init() {
        userUuid = usersRepository.createUser("text@example.com", "abc", "Test user")
                .userUuid();
        teamUuid = teamsRepository.createTeam(userUuid, "Test team").teamUuid();

    }

    @Test
    void getTask_teamNotFoundException() {
        assertThrows(TeamNotFoundException.class,
                () -> tasksService.getTask(UUID.randomUUID(), UUID.randomUUID(), teamUuid));
    }

    @Test
    void getTask_taskNotFoundException() {
        assertThrows(TaskNotFoundException.class,
                () -> tasksService.getTask(UUID.randomUUID(), userUuid, teamUuid));
    }

    @Test
    void createAndGetTask_success() {
        final var expected = tasksService.createTask(userUuid, "Test task",
                "http://google.com:1234/task?param=123#test", Scale.FIBONACCI, teamUuid);
        final var actual = tasksService.getTask(expected.taskUuid(), expected.userUuid(), teamUuid);
        assertEquals(expected, actual);
    }

    @Test
    void createAndGetTask_invalidDomain_errorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> tasksService.createTask(
                userUuid,
                RandomStringUtils.random(TasksService.TASK_NAME_NAME_MAX_LENGTH + 1),
                "http://google.comm:1234/task?param=123#test",
                Scale.FIBONACCI,
                teamUuid
        ));
        assertEquals(ErrorStatus.INVALID_TASK_NAME, ex.getStatus());
    }

    @Test
    void createAndGetTask_tooLongTaskName_errorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> tasksService.createTask(
                userUuid,
                RandomStringUtils.random(TasksService.TASK_NAME_NAME_MAX_LENGTH + 1),
                "http://google.com:1234/task?param=123#test",
                Scale.FIBONACCI,
                teamUuid
        ));
        assertEquals(ErrorStatus.INVALID_TASK_NAME, ex.getStatus());
    }

    @Test
    void createAndGetTask_tooLongUrl_errorStatusException() {
        final String validUrl = "http://google.com:1234/task?param=123";
        final var ex = assertThrows(ErrorStatusException.class, () -> tasksService.createTask(
                userUuid,
                "Test task",
                validUrl + RandomStringUtils.random(TasksService.TASK_NAME_NAME_MAX_LENGTH - validUrl.length() + 1),
                Scale.FIBONACCI,
                teamUuid
        ));
        assertEquals(ErrorStatus.INVALID_TASK_URL, ex.getStatus());
    }

    @Test
    void createAndFinishTask_success() {
        final var expected = tasksService.createTask(userUuid, "Test task",
                "http://google.com:1234/task?param=123#test", Scale.FIBONACCI, teamUuid);
        tasksService.finishTask(expected.taskUuid(), expected.userUuid(), teamUuid);
        final var actual = tasksService.getTask(expected.taskUuid(), expected.userUuid(), teamUuid);
        assertTrue(actual.finished());
    }

    @Test
    void createAndDeleteTask_success() {
        final var expected = tasksService.createTask(userUuid, "Test task",
                "http://google.com:1234/task?param=123#test", Scale.FIBONACCI, teamUuid);
        tasksService.deleteTask(expected.taskUuid(), expected.userUuid(), teamUuid);
        assertThrows(TaskNotFoundException.class,
                () -> tasksService.getTask(expected.taskUuid(), expected.userUuid(), teamUuid));
    }

    @Test
    void getTasks_withFilterByUrl_success() {
        tasksService.createTask(userUuid, "Test task",
                "http://ooglE.com", Scale.FIBONACCI, teamUuid);
        final var expected = tasksService.createTask(userUuid, "Test task",
                "http://yahoO.com", Scale.FIBONACCI, teamUuid);

        final var actual = tasksService.getTasks(userUuid, teamUuid, "Yahoo");
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void getTasks_withFilterByName_success() {
        final var expected = tasksService.createTask(userUuid, "Yahoo task",
                "http://example.com", Scale.FIBONACCI, teamUuid);
        tasksService.createTask(userUuid, "Google task",
                "http://example.com", Scale.FIBONACCI, teamUuid);

        final var actual = tasksService.getTasks(userUuid, teamUuid, "yahoO");
        assertThat(actual).containsExactly(expected);
    }
}
