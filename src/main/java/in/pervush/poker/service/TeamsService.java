package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.MembershipNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.exception.UserAlreadyAddedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamsService {

    public static final int TEAM_NAME_NAME_MAX_LENGTH = 32;
    private final TeamsRepository teamsRepository;
    private final UsersRepository usersRepository;

    public DBUserTeam createTeam(final UUID userUuid, final String teamName) {
        validateTeamName(teamName);
        return teamsRepository.createTeam(userUuid, teamName);
    }

    public void deleteTeam(final UUID teamUuid, final UUID userUuid) throws ForbiddenException {
        teamsRepository.deleteTeam(teamUuid, userUuid);
    }

    public List<DBUserTeam> getTeams(final UUID userUuid, @Nullable MembershipStatus membershipStatus) {
        return teamsRepository.getUserTeams(userUuid, membershipStatus);
    }

    public List<DBUserTeam> getTeamMembers(final UUID teamUuid, final UUID userUuid) {
        validateTeamMember(teamUuid, userUuid);
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
    }

    public void deleteTeamMember(final UUID teamUuid, final UUID teamOwnerUserUuid, final UUID deletingUserUuid)
            throws ForbiddenException, MembershipNotFoundException {
        validateTeamOwner(teamUuid, teamOwnerUserUuid);
        teamsRepository.deleteTeamMember(teamUuid, deletingUserUuid);
    }

    public void validateTeamMember(final UUID teamUuid, final UUID userUuid) throws TeamNotFoundException {
        final var team = teamsRepository.getTeam(teamUuid, userUuid);
        if (team.membershipStatus() != MembershipStatus.MEMBER && team.membershipStatus() != MembershipStatus.OWNER) {
            throw new TeamNotFoundException();
        }
    }

    private void validateTeamOwner(final UUID teamUuid, final UUID teamOwnerUserUuid) throws ForbiddenException {
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
