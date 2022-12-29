package in.pervush.poker.model.tasks;

import in.pervush.poker.model.votes.VoteValue;

import java.time.Instant;
import java.util.UUID;

public record DBTask(UUID taskUuid, UUID userUuid, String name, String url, Scale scale, boolean finished,
                     Instant createDtm, VoteValue voteValue, UUID teamUuid) {
}
