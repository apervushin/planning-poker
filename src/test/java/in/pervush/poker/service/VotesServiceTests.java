package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TaskNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.model.votes.DBUserVoteStat;
import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.VotesRepository;
import in.pervush.poker.repository.postgres.UsersMapper;
import in.pervush.poker.utils.InstantUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({VotesService.class, UsersRepository.class, TasksRepository.class, TasksService.class,
        TeamsRepository.class, TeamsService.class, VotesRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@Transactional
public class VotesServiceTests {

    private static final String USER_EMAIL = "test@example.com";
    private static final String USER_PASSWORD = "abc";
    private static final String USER_NAME = "Test user";
    private final UUID userUuid = UUID.randomUUID();
    private UUID taskUuid;
    private UUID teamUuid;

    @Autowired
    private VotesService service;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private TasksService tasksService;

    @Autowired
    private TeamsRepository teamsRepository;

    @BeforeEach
    void initUserAndTask() {
        usersMapper.createUser(userUuid, USER_EMAIL, USER_PASSWORD, USER_NAME, InstantUtils.now());
        this.teamUuid = teamsRepository.createTeam(userUuid, "Test team").teamUuid();
        this.taskUuid = tasksService.createTask(userUuid, "Test task", "http://google.com", Scale.FIBONACCI,
                teamUuid).taskUuid();

    }

    @Test
    void createVote() {
        final var ex = assertThrows(ErrorStatusException.class,
                () -> service.createVote(taskUuid, teamUuid, userUuid, VoteValue.SIZE_XS));
        assertEquals(ErrorStatus.INVALID_VOTE_VALUE, ex.getStatus());
    }

    @Test
    void getVotesStat_invalidTaskStatus() {
        service.createVote(taskUuid, teamUuid, userUuid, VoteValue.VALUE_3);
        final var ex = assertThrows(ErrorStatusException.class,
                () -> service.getVotes(taskUuid, teamUuid, userUuid));
        assertEquals(ErrorStatus.INVALID_TASK_STATUS, ex.getStatus());
    }

    @Test
    void getVotes_success() {
        // create second user
        final String user2Name = "qwerty1";
        final UUID user2Uuid = UUID.fromString("03356451-decf-44ba-8eaa-3c320a946001");
        usersMapper.createUser(user2Uuid, "test1@example.com", USER_PASSWORD, user2Name, InstantUtils.now());
        teamsRepository.addTeamMember(teamUuid, user2Uuid, MembershipStatus.MEMBER);

        // create third user
        final String user3Name = "qwerty2";
        final UUID user3Uuid = UUID.fromString("3cb4a61d-ea90-485b-b43e-c8d51f66282d");
        usersMapper.createUser(user3Uuid, "test2@example.com", USER_PASSWORD, user3Name, InstantUtils.now());
        teamsRepository.addTeamMember(teamUuid, user3Uuid, MembershipStatus.MEMBER);

        // create votes
        service.createVote(taskUuid, teamUuid, userUuid, VoteValue.VALUE_3);
        service.createVote(taskUuid, teamUuid, userUuid, VoteValue.VALUE_5); // vote with another value
        service.createVote(taskUuid, teamUuid, user2Uuid, VoteValue.VALUE_1);
        service.createVote(taskUuid, teamUuid, user3Uuid, VoteValue.VALUE_1);

        // finish task
        tasksService.finishTask(taskUuid, userUuid, teamUuid);

        // validate
        final var expected = List.of(
                new DBVote(user2Uuid, VoteValue.VALUE_1),
                new DBVote(user3Uuid, VoteValue.VALUE_1),
                new DBVote(userUuid, VoteValue.VALUE_5)
        );
        assertEquals(expected, service.getVotes(taskUuid, teamUuid, userUuid));
    }

    @Test
    void getVotesStat_notFoundException() {
        service.createVote(taskUuid, teamUuid, userUuid, VoteValue.VALUE_3);
        tasksService.deleteTasks(Set.of(taskUuid), userUuid, teamUuid);
        assertThrows(TaskNotFoundException.class, () -> service.getVotes(taskUuid, teamUuid, userUuid));
    }

    @Test
    void getVotesStat_success() {
        // create second task
        final var task2Uuid = tasksService
                .createTask(userUuid, "Test task", "http://google.com/1", Scale.FIBONACCI, teamUuid).taskUuid();
        // create third task
        final var task3Uuid = tasksService
                .createTask(userUuid, "Test task", "http://google.com/2", Scale.FIBONACCI, teamUuid).taskUuid();

        // create second user
        final String user2Name = "qwerty1";
        final UUID user2Uuid = UUID.randomUUID();
        usersMapper.createUser(user2Uuid, "test1@example.com", USER_PASSWORD, user2Name, InstantUtils.now());
        teamsRepository.addTeamMember(teamUuid, user2Uuid, MembershipStatus.MEMBER);

        // create votes for tasks 1,2,3
        service.createVote(taskUuid, teamUuid, userUuid, VoteValue.VALUE_3);
        service.createVote(task2Uuid, teamUuid, userUuid, VoteValue.VALUE_3);
        service.createVote(task2Uuid, teamUuid, user2Uuid, VoteValue.VALUE_1);
        service.createVote(task3Uuid, teamUuid, user2Uuid, VoteValue.VALUE_1);

        // finish tasks 1,2
        tasksService.finishTask(taskUuid, userUuid, teamUuid);
        tasksService.finishTask(task2Uuid, userUuid, teamUuid);

        final List<DBUserVoteStat> expected = List.of(
                new DBUserVoteStat(userUuid, 2),
                new DBUserVoteStat(user2Uuid, 1)
        );
        final var actual = service.getVotesStat(teamUuid, userUuid,
                InstantUtils.now().minus(Duration.of(1, ChronoUnit.HOURS)), Instant.now());

        assertEquals(expected, actual);
    }

    @Test
    void getVotesStat_notTeamMember_teamNotFoundException() {
        assertThrows(TeamNotFoundException.class, () -> service.getVotesStat(teamUuid, UUID.randomUUID(),
                InstantUtils.now(), Instant.now()));
    }
}
