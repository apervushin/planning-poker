package in.pervush.poker.model.votes;

import java.util.UUID;

public record DBVote(UUID userUuid, VoteValue vote) {
}
