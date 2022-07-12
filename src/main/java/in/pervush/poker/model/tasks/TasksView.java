package in.pervush.poker.model.tasks;

import in.pervush.poker.model.user.DBUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TasksView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) Status status,
        @Schema(required = true) String userName) {

    public static TasksView of(DBTask dbTask, DBUser dbUser) {
        return new TasksView(dbTask.taskUuid(), dbTask.name(), dbTask.url(), dbTask.scale(), dbTask.status(),
                dbUser.name());
    }
}
