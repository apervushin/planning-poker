package in.pervush.poker.model.tasks;

import in.pervush.poker.model.votes.VoteValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TaskView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) UUID userUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean finished,
        @Schema(description = "The requesting user' vote value") VoteValue voteValue,
        @Schema(required = true) int votesCount) {

    public static TaskView of(final DBTask dbTask) {
        return new TaskView(dbTask.taskUuid(), dbTask.userUuid(), dbTask.name(), dbTask.url(), dbTask.scale(),
                dbTask.finished(), dbTask.voteValue(), dbTask.votesCount());
    }
}
