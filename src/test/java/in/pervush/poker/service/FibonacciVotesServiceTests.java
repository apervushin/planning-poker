package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({FibonacciVotesService.class, UsersRepository.class, TasksRepository.class, TasksService.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@Transactional
public class FibonacciVotesServiceTests {

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "abc";
    private static final String USER_NAME = "Test user";
    private UUID taskUuid;
    private UUID userUuid;

    @Autowired
    private FibonacciVotesService service;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TasksService tasksService;

    @BeforeEach
    void initUserAndTask() {
        userUuid = usersRepository.createUser(USER_EMAIL, USER_PASSWORD, USER_NAME).userUuid();
        taskUuid = tasksService.createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI)
                .taskUuid();
    }

    @Test
    void createVote() {
        final var ex = assertThrows(ErrorStatusException.class,
                () -> service.createVote(taskUuid, userUuid, VoteValue.SIZE_XS));
        assertEquals(ErrorStatus.INVALID_VOTE_VALUE, ex.getStatus());
    }

    @Test
    void getVotesStat_invalidTaskStatus() {
        service.createVote(taskUuid, userUuid, VoteValue.VALUE_3);
        final var ex = assertThrows(ErrorStatusException.class, () -> service.getVotesStat(taskUuid));
        assertEquals(ErrorStatus.INVALID_TASK_STATUS, ex.getStatus());
    }

    @Test
    void getVotesStat_success() {
        // create second user
        final String user2Name = "qwerty1";
        final var user2Uuid = usersRepository.createUser("test1@example.com", USER_PASSWORD, user2Name)
                .userUuid();

        // create third user
        final String user3Name = "qwerty2";
        final var user3Uuid = usersRepository.createUser("test2@example.com", USER_PASSWORD, user3Name)
                .userUuid();

        // create votes
        service.createVote(taskUuid, userUuid, VoteValue.VALUE_3);
        service.createVote(taskUuid, userUuid, VoteValue.VALUE_5); // vote with another value
        service.createVote(taskUuid, user2Uuid, VoteValue.VALUE_1);
        service.createVote(taskUuid, user3Uuid, VoteValue.VALUE_1);

        // finish task
        tasksService.finishTask(taskUuid, userUuid);

        // validate
        final var expected = Map.of(
                VoteValue.VALUE_1, List.of(user2Name, user3Name),
                VoteValue.VALUE_5, List.of(USER_NAME)
        );
        assertEquals(expected, service.getVotesStat(taskUuid));
    }

    @Test
    void getVotesStat_notFoundException() {
        service.createVote(taskUuid, userUuid, VoteValue.VALUE_3);
        tasksService.deleteTask(taskUuid, userUuid);
        assertThrows(NotFoundException.class, () -> service.getVotesStat(taskUuid));
    }
}
