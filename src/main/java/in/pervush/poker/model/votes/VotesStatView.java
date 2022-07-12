package in.pervush.poker.model.votes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record VotesStatView(
        @Schema(required = true)
        String value,
        @Schema(required = true)
        List<String> userNames) {
}
