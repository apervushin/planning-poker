package in.pervush.poker.model.tasks;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TasksListItemView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean active,
        @Schema(required = true) int votesCount) {

    public static TasksListItemView of(final DBTask dbTask) {
        return new TasksListItemView(dbTask.taskUuid(), dbTask.name(), dbTask.url(), dbTask.scale(),
                dbTask.status() == Status.ACTIVE, dbTask.votesCount());
    }
}
