package in.pervush.poker.service;

import in.pervush.poker.configuration.tests.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TaskUrlExistsException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.VotesRepository;
import in.pervush.poker.utils.InstantUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig({UsersRepository.class, TasksService.class, TasksRepository.class, TeamsRepository.class,
        TeamsService.class, VotesRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class})
@Transactional
@EnableAsync
public class TasksServiceTests {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksService tasksService;

    @Autowired
    private TeamsRepository teamsRepository;

    @Autowired
    private VotesRepository votesRepository;

    private UUID userUuid;
    private UUID teamUuid;

    @BeforeEach
    void init() {
        userUuid = usersRepository.createUser("text@example.com", "Test user")
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
        System.out.println("TEST");
        assertEquals(expected, actual);
    }

    @Test
    void createAndGetTask_withVote_success() {
        final var task = tasksService.createTask(userUuid, "Test task",
                "http://google.com:1234/task?param=123#test", Scale.FIBONACCI, teamUuid);
        final var voteValue = VoteValue.VALUE_8;
        votesRepository.createVote(task.taskUuid(), userUuid, voteValue);
        final var expected = new DBTask(
                task.taskUuid(),
                task.userUuid(),
                task.name(),
                task.url(),
                task.scale(),
                task.finished(),
                task.createDtm(),
                voteValue,
                task.teamUuid()
        );
        final var actual = tasksService.getTask(task.taskUuid(), task.userUuid(), teamUuid);
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
        assertEquals(ErrorStatus.INVALID_TASK_NAME, ex.status);
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
        assertEquals(ErrorStatus.INVALID_TASK_NAME, ex.status);
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
        assertEquals(ErrorStatus.INVALID_TASK_URL, ex.status);
    }

    @Test
    void createTask_duplicateUrl_taskUrlExistsException() {
        tasksService.createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI, teamUuid);
        assertThrows(TaskUrlExistsException.class, () -> tasksService
                .createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI, teamUuid));
    }

    @Test
    void createTask_duplicateUrlDeletedTask_noException() {
        final var task = tasksService
                .createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI, teamUuid);
        tasksService.deleteTasks(Set.of(task.taskUuid()), userUuid, teamUuid);
        assertDoesNotThrow(() -> tasksService
                .createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI, teamUuid));
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
        tasksService.deleteTasks(Set.of(expected.taskUuid()), expected.userUuid(), teamUuid);
        assertThrows(TaskNotFoundException.class,
                () -> tasksService.getTask(expected.taskUuid(), expected.userUuid(), teamUuid));
    }

    @Test
    void getTasks_withFilterByUrl_success() {
        tasksService.createTask(userUuid, "Test task",
                "http://ooglE.com", Scale.FIBONACCI, teamUuid);
        final var expected = tasksService.createTask(userUuid, "Test task",
                "http://yahoO.com", Scale.FIBONACCI, teamUuid);

        final var actual = tasksService.getTasks(userUuid, teamUuid, "Yahoo", null);
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void getTasks_withFilterByName_success() {
        final var expected = tasksService.createTask(userUuid, "Yahoo task",
                "http://example.com/1", Scale.FIBONACCI, teamUuid);
        tasksService.createTask(userUuid, "Google task",
                "http://example.com/2", Scale.FIBONACCI, teamUuid);

        final var actual = tasksService.getTasks(userUuid, teamUuid, "yahoO", null);
        assertThat(actual).containsExactly(expected);
    }

    @Test
    void getTasks_notFinished_success() {
        final var task1 = tasksService.createTask(userUuid, "Test task",
                "http://ooglE.com", Scale.FIBONACCI, teamUuid);
        final var task2 = tasksService.createTask(userUuid, "Test task",
                "http://yahoO.com", Scale.FIBONACCI, teamUuid);
        tasksService.finishTask(task2.taskUuid(), userUuid, teamUuid);

        final var actual = tasksService.getTasks(userUuid, teamUuid, null, false);
        assertThat(actual).containsExactly(task1);
    }

    @Test
    void getFinishedTasksCount_success() {
        tasksService.createTask(userUuid, "Test task",
                "http://ooglE.com", Scale.FIBONACCI, teamUuid);
        final var task2 = tasksService.createTask(userUuid, "Test task",
                "http://yahoO.com", Scale.FIBONACCI, teamUuid);
        tasksService.finishTask(task2.taskUuid(), userUuid, teamUuid);

        final int expected = 1;
        final int actual = tasksService.getFinishedTasksCount(teamUuid, userUuid,
                InstantUtils.now().minus(Duration.of(1, ChronoUnit.DAYS)), InstantUtils.now());

        assertEquals(expected, actual);
    }

    @Test
    void activateTask_success() {
        final var task = tasksService
                .createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI, teamUuid);
        votesRepository.createVote(task.taskUuid(), userUuid, VoteValue.VALUE_8);
        tasksService.finishTask(task.taskUuid(), userUuid, teamUuid);

        tasksService.activateTask(task.taskUuid(), userUuid, teamUuid);

        assertFalse(tasksService.getTask(task.taskUuid(), userUuid, teamUuid).finished());
        assertThat(votesRepository.getVotes(task.taskUuid())).isEmpty();
    }

    @Test
    void getFinishedTasksCount_notTeamMember_teamNotFoundException() {
        assertThrows(TeamNotFoundException.class, () -> tasksService.getFinishedTasksCount(teamUuid, UUID.randomUUID(),
                InstantUtils.now(), InstantUtils.now()));
    }
}
