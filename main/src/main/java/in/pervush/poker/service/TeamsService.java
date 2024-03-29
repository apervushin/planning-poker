package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.MembershipNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.exception.UserAlreadyAddedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.events.TeamCreatedEvent;
import in.pervush.poker.model.events.TeamDeletedEvent;
import in.pervush.poker.model.events.UserJoinedTeamEvent;
import in.pervush.poker.model.events.UserLeftTeamEvent;
import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class TeamsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TeamsService.class);

    public static final int TEAM_NAME_NAME_MAX_LENGTH = 32;
    private final TeamsRepository teamsRepository;
    private final UsersRepository usersRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TeamsService(TeamsRepository teamsRepository, UsersRepository usersRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.teamsRepository = teamsRepository;
        this.usersRepository = usersRepository;
        this.eventPublisher = eventPublisher;
    }

    public DBUserTeam createTeam(final UUID userUuid, final String teamName) {
        validateTeamName(teamName);
        final var team = teamsRepository.createTeam(userUuid, teamName);
        final var event = new TeamCreatedEvent(team.teamUuid(), team.teamName(), team.userUuid());
        eventPublisher.publishEvent(event);
        log.debug("Published event: {}", event);
        return team;
    }

    public void deleteTeam(final UUID teamUuid, final UUID userUuid) throws ForbiddenException {
        teamsRepository.deleteTeam(teamUuid, userUuid);
        eventPublisher.publishEvent(new TeamDeletedEvent(teamUuid));
    }

    public List<DBUserTeam> getTeams(final UUID userUuid, @Nullable MembershipStatus membershipStatus) {
        return teamsRepository.getUserTeams(userUuid, membershipStatus);
    }

    public List<DBUserTeam> getTeamMembers(final UUID teamUuid, final UUID userUuid) throws TeamNotFoundException {
        validateTeamMemberAndGetTeam(teamUuid, userUuid);
        return teamsRepository.getTeamMembers(teamUuid);
    }

    public void inviteTeamMember(final UUID teamUuid, final UUID teamOwnerUserUuid, final String email)
            throws UserAlreadyAddedException, ForbiddenException, UserNotFoundException {
        validateTeamOwner(teamUuid, teamOwnerUserUuid);
        final var user = usersRepository.getUser(email);
        teamsRepository.addTeamMember(teamUuid, user.userUuid(), MembershipStatus.INVITED);
    }

    public void acceptTeamInvitation(final UUID teamUuid, final UUID userUuid) throws MembershipNotFoundException {
        teamsRepository.setMembershipStatus(teamUuid, userUuid, MembershipStatus.MEMBER, MembershipStatus.INVITED);
        eventPublisher.publishEvent(new UserJoinedTeamEvent(teamUuid, userUuid));
    }

    public void deleteTeamMember(final UUID teamUuid, final UUID userUuid, final UUID deletingUserUuid)
            throws ForbiddenException, MembershipNotFoundException {
        validateTeamOwnerOrUserUuidsEquals(teamUuid, userUuid, deletingUserUuid);
        teamsRepository.deleteTeamMember(teamUuid, deletingUserUuid);
        eventPublisher.publishEvent(new UserLeftTeamEvent(teamUuid, userUuid));
    }

    public DBUserTeam validateTeamMemberAndGetTeam(final UUID teamUuid, final UUID userUuid)
            throws TeamNotFoundException {

        final var team = teamsRepository.getTeam(teamUuid, userUuid);
        if (team.membershipStatus() != MembershipStatus.MEMBER && team.membershipStatus() != MembershipStatus.OWNER) {
            throw new TeamNotFoundException();
        }
        return team;
    }

    private void validateTeamOwnerOrUserUuidsEquals(final UUID teamUuid, final UUID userUuid,
                                                    final UUID deletingUserUuid) {
        if (!Objects.equals(userUuid, deletingUserUuid)) {
            validateTeamOwner(teamUuid, userUuid);
        }
    }

    private void validateTeamOwner(final UUID teamUuid, final UUID teamOwnerUserUuid)
            throws ForbiddenException {
        try {
            if(teamsRepository.getTeam(teamUuid, teamOwnerUserUuid).membershipStatus() != MembershipStatus.OWNER) {
                throw new ForbiddenException();
            }
        } catch (TeamNotFoundException ex) {
            throw new ForbiddenException();
        }
    }

    private static void validateTeamName(final String name) {
        if (Strings.isBlank(name) || name.length() > TEAM_NAME_NAME_MAX_LENGTH) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TEAM_NAME);
        }
    }
}
