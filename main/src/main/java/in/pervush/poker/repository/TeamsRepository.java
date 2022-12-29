package in.pervush.poker.repository;

import in.pervush.poker.exception.ForbiddenException;
import in.pervush.poker.exception.MembershipNotFoundException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.exception.UserAlreadyAddedException;
import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.repository.postgres.TeamsMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TeamsRepository {

    private final TeamsMapper mapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public DBUserTeam createTeam(final UUID ownerUserUuid, final String teamName) {
        final var createDtm = InstantUtils.now();
        final var status = MembershipStatus.OWNER;
        final var teamUuid = UUID.randomUUID();

        mapper.createTeam(teamUuid, teamName, createDtm);
        mapper.addTeamMember(teamUuid, ownerUserUuid, createDtm, MembershipStatus.OWNER);

        return new DBUserTeam(teamUuid, teamName, ownerUserUuid, createDtm, status);
    }

    public void deleteTeam(final UUID teamUuid, final UUID userUuid) throws ForbiddenException {
        final boolean updated = mapper.deleteTeam(teamUuid, userUuid);
        if (!updated) {
            throw new ForbiddenException();
        }
    }

    public List<DBUserTeam> getUserTeams(final UUID userUuid, @Nullable final MembershipStatus membershipStatus) {
        if (membershipStatus == null) {
            return mapper.getUserTeamsByUserUuid(userUuid);
        }
        return mapper.getUserTeamsByUserUuidAndMembershipStatus(userUuid, membershipStatus);
    }

    public void addTeamMember(final UUID teamUuid, final UUID userUuid, final MembershipStatus membershipStatus)
            throws UserAlreadyAddedException {
        final var createDtm = InstantUtils.now();
        try {
            mapper.addTeamMember(teamUuid, userUuid, createDtm, membershipStatus);
        } catch (DuplicateKeyException ex) {
            throw new UserAlreadyAddedException();
        }
    }

    public DBUserTeam getTeam(final UUID teamUuid, final UUID userUuid) throws TeamNotFoundException {
        return mapper.getNotDeletedTeam(teamUuid, userUuid).orElseThrow(TeamNotFoundException::new);
    }

    public List<DBUserTeam> getTeamMembers(final UUID teamUuid) {
        return mapper.getTeamMembers(teamUuid);
    }

    public void setMembershipStatus(final UUID teamUuid, final UUID userUuid,
                                    final MembershipStatus newMembershipStatus,
                                    final MembershipStatus oldMembershipStatus) throws MembershipNotFoundException {
        final boolean updated = mapper.setMembershipStatus(teamUuid, userUuid, newMembershipStatus,
                oldMembershipStatus);
        if (!updated) {
            throw new MembershipNotFoundException();
        }
    }

    public void deleteTeamMember(final UUID teamUuid, final UUID userUuid) throws MembershipNotFoundException {
        final var deleted = mapper.deleteTeamMember(teamUuid, userUuid);
        if (!deleted) {
            throw new MembershipNotFoundException();
        }
    }
}
