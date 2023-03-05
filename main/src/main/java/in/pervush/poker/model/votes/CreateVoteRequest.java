package in.pervush.poker.model.votes;

import jakarta.validation.constraints.NotNull;

public record CreateVoteRequest(
        @NotNull
        VoteValue value
) {
}
