package in.pervush.poker.repository;

import in.pervush.poker.model.votes.DBUserVoteStat;
import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.postgres.VotesMapper;
import in.pervush.poker.utils.InstantUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class VotesRepository {

    private final VotesMapper mapper;

    public VotesRepository(VotesMapper mapper) {
        this.mapper = mapper;
    }

    public void createVote(final UUID taskUuid, final UUID votingUserUuid, final VoteValue vote) {
        mapper.createVote(taskUuid, votingUserUuid, vote, InstantUtils.now());
    }

    public List<DBVote> getVotes(final UUID taskUuid) {
        return mapper.getVotes(taskUuid);
    }

    public List<DBUserVoteStat> getVotesStat(final UUID teamUuid, final Instant startDtm, final Instant endDtm) {
        return mapper.getVotesStat(teamUuid, startDtm, endDtm);
    }

    public void eraseVotes(final UUID taskUuid) {
        mapper.eraseVotes(taskUuid);
    }

    public int countNotVotedUserTasks(UUID teamUuid, UUID userUuid) {
        return mapper.countNotVotedUserTasks(teamUuid, userUuid);
    }
}
