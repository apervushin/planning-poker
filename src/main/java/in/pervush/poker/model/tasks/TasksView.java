package in.pervush.poker.model.tasks;

import in.pervush.poker.model.user.DBUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TasksView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean finished,
        @Schema(required = true) String userName,
        @Schema(required = true) int votesCount) {

    public static TasksView of(final DBTask dbTask, final DBUser dbUser) {
        return new TasksView(dbTask.taskUuid(), dbTask.name(), dbTask.url(), dbTask.scale(),
                dbTask.finished(), dbUser.name(), dbTask.votesCount());
    }
}
