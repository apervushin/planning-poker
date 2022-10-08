package in.pervush.poker.model.votes;

import in.pervush.poker.model.user.UserPublicView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record VotesStatView(
        @Schema(required = true)
        VoteValue value,
        @Deprecated @Schema(required = true, deprecated = true)
        List<String> userNames,
        List<UserPublicView> votedUsers) {
}
