package in.pervush.poker.model.tasks;

import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserPublicView;
import in.pervush.poker.model.votes.VoteValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record TaskView(
        @Schema(required = true) UUID taskUuid,
        @Schema(required = true) UserPublicView taskOwner,
        @Schema(required = true) String name,
        String url,
        @Schema(required = true) Scale scale,
        @Schema(required = true) boolean finished,
        @Schema(description = "The requesting user' vote value") VoteValue voteValue,
        List<UserPublicView> votedUsers
) {

    public static TaskView of(final DBTask dbTask, final DBUser dbUser,
                              final List<DBUser> votedUsers) {
        return new TaskView(dbTask.taskUuid(), UserPublicView.of(dbUser),
                dbTask.name(), dbTask.url(), dbTask.scale(), dbTask.finished(), dbTask.voteValue(),
                votedUsers.stream().map(UserPublicView::of).collect(Collectors.toList()));
    }
}
