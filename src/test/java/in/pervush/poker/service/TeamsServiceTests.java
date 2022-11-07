package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.MembershipNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

@SpringJUnitConfig({UsersRepository.class, TeamsService.class, TeamsRepository.class, UsersRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@Transactional
public class TeamsServiceTests {

    @Autowired
    private TeamsService service;
    @Autowired
    private TeamsRepository repository;
    @Autowired
    private UsersRepository usersRepository;

    private DBUser user;

    @BeforeEach
    void init() {
        user = usersRepository.createUser("text@example.com", "abc", "Test user");
    }

    @Test
    void createTeam_success() {
        final var expected = new DBUserTeam(UUID.randomUUID(), "test", user.userUuid(), Instant.now(),
                MembershipStatus.OWNER);
        final var actual = service.createTeam(user.userUuid(), expected.teamName());
        assertThat(actual).usingRecursiveComparison().ignoringFields("teamUuid", "teamCreateDtm")
                .isEqualTo(expected);
    }

    @Test
    void createTeam_tooLongName_errorStatusException() {
        final var exception = assertThrows(ErrorStatusException.class, () -> service.createTeam(
                user.userUuid(),
                RandomStringUtils.random(TeamsService.TEAM_NAME_NAME_MAX_LENGTH + 1)
        ));
        Assertions.assertEquals(ErrorStatus.INVALID_TEAM_NAME, exception.getStatus());
    }

    @Test
    void inviteTeamMember_success() {
        final var user = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team = service.createTeam(this.user.userUuid(), "Test team");
        service.inviteTeamMember(team.teamUuid(), this.user.userUuid(), user.email());
        final var membershipStatus = repository.getTeam(team.teamUuid(), user.userUuid()).membershipStatus();
        Assertions.assertEquals(MembershipStatus.INVITED, membershipStatus);
    }

    @Test
    void inviteTeamMember_teamNotExists_forbiddenException() {
        final var user = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team = service.createTeam(this.user.userUuid(), "Test team");
        service.inviteTeamMember(team.teamUuid(), this.user.userUuid(), user.email());

        assertThrows(ForbiddenException.class,
                () -> service.inviteTeamMember(UUID.randomUUID(), this.user.userUuid(), user.email()));
    }

    @Test
    void inviteTeamMember_userIsNotTeamOwner_forbiddenException() {
        final var user = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team = service.createTeam(this.user.userUuid(), "Test team");

        assertThrows(ForbiddenException.class, () -> service.inviteTeamMember(team.teamUuid(), user.userUuid(), user.email()));
    }

    @Test
    void acceptTeamInvitation_success() {
        final var user = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team = service.createTeam(this.user.userUuid(), "Test team");
        service.inviteTeamMember(team.teamUuid(), this.user.userUuid(), user.email());

        service.acceptTeamInvitation(team.teamUuid(), user.userUuid());
        final var membershipStatus = repository.getTeam(team.teamUuid(), user.userUuid()).membershipStatus();
        Assertions.assertEquals(MembershipStatus.MEMBER, membershipStatus);
    }

    @Test
    void acceptTeamInvitation_noInvitationExists_success() {
        final var user = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team = service.createTeam(this.user.userUuid(), "Test team");

        assertThrows(MembershipNotFoundException.class,
                () -> service.acceptTeamInvitation(team.teamUuid(), user.userUuid()));
    }

    @Test
    void getTeams_withMembershipStatusFilter_success() {
        final var team1 = service.createTeam(user.userUuid(), "Test team 1");
        final var team2 = service.createTeam(user.userUuid(), "Test team 2");

        final var actual = service.getTeams(user.userUuid(), MembershipStatus.OWNER);

        assertThat(List.of(team1, team2)).isEqualTo(actual);
    }

    @Test
    void getTeams_withoutMembershipStatusFilter_success() {
        final var team1 = service.createTeam(user.userUuid(), "Test team 1");

        final var user2 = usersRepository.createUser("test@example.com", "pswd", "Test user");
        final var team2 = service.createTeam(user2.userUuid(), "Test team");
        service.inviteTeamMember(team2.teamUuid(), user2.userUuid(), user.email());
        service.acceptTeamInvitation(team2.teamUuid(), this.user.userUuid());

        final var actual = service.getTeams(this.user.userUuid(), null);

        assertThat(List.of(team1, repository.getTeam(team2.teamUuid(), this.user.userUuid()))).isEqualTo(actual);
    }

    @Test
    void deleteTeam_success() {
        final var team1 = service.createTeam(user.userUuid(), "Test team 1");
        service.deleteTeam(team1.teamUuid(), user.userUuid());
        assertThrows(TeamNotFoundException.class, () -> repository.getTeam(team1.teamUuid(), user.userUuid()));
    }

    @Test
    void deleteTeam_userIsNotTeamOwner_forbiddenException() {
        assertThrows(ForbiddenException.class, () -> service.deleteTeam(UUID.randomUUID(), user.userUuid()));
    }

    @Test
    void getTeamMembers_success() {
        final var team = service.createTeam(user.userUuid(), "Test team 1");
        final var user2 = usersRepository.createUser("text1@example.com", "abc", "Test user");
        service.inviteTeamMember(team.teamUuid(), user.userUuid(), user2.email());
        final var team2 = repository.getTeam(team.teamUuid(), user2.userUuid());

        final var actual = service.getTeamMembers(team.teamUuid(), user.userUuid());
        assertThat(actual).isEqualTo(List.of(team, team2));
    }

    @Test
    void deleteTeamMember_success() {
        final var team = service.createTeam(user.userUuid(), "Test team 1");
        final var user2 = usersRepository.createUser("text1@example.com", "abc", "Test user");
        service.inviteTeamMember(team.teamUuid(), user.userUuid(), user2.email());

        service.deleteTeamMember(team.teamUuid(), user.userUuid(), user2.userUuid());
    }

    @Test
    void deleteTeamMember_userIsNotTeamOwner_forbiddenException() {
        final var team = service.createTeam(user.userUuid(), "Test team 1");
        final var user2 = usersRepository.createUser("text1@example.com", "abc", "Test user");
        service.inviteTeamMember(team.teamUuid(), user.userUuid(), user2.email());

        assertThrows(ForbiddenException.class,
                () -> service.deleteTeamMember(team.teamUuid(), user2.userUuid(), user2.userUuid()));
    }

    @Test
    void deleteTeamMember_deletingTeamOwnerByTeamOwner_membershipNotFoundException() {
        final var team = service.createTeam(user.userUuid(), "Test team 1");

        assertThrows(MembershipNotFoundException.class, () -> service.deleteTeamMember(team.teamUuid(), user.userUuid(), user.userUuid()));
    }
}
