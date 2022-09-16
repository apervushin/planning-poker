package in.pervush.poker.model.tasks;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TaskView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) UUID userUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean finished,
        @Schema(required = true, deprecated = true) @Deprecated String userName,
        @Schema(required = true) boolean voted,
        @Schema(required = true) int votesCount) {

    public static TaskView of(final DBTask dbTask) {
        return new TaskView(dbTask.taskUuid(), dbTask.userUuid(), dbTask.name(), dbTask.url(), dbTask.scale(),
                dbTask.finished(), dbTask.userUuid().toString(), dbTask.voted(), dbTask.votesCount());
    }
}
