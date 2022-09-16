package in.pervush.poker.model.tasks;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TasksView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) UUID userUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean finished,
        @Schema(required = true, deprecated = true) @Deprecated String userName,
        @Schema(required = true) int votesCount) {

    public static TasksView of(final DBTask dbTask) {
        return new TasksView(dbTask.taskUuid(), dbTask.userUuid(), dbTask.name(), dbTask.url(), dbTask.scale(),
                dbTask.finished(), dbTask.userUuid().toString(), dbTask.votesCount());
    }
}
