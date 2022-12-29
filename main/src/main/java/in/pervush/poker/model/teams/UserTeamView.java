package in.pervush.poker.model.teams;

import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserPublicView;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserTeamView (
        @Schema(required = true) UUID teamUuid,
        @Schema(required = true) String teamName,
        @Schema(required = true) UserPublicView user,
        @Schema(required = true) MembershipStatus membershipStatus,
        @Schema(required = true) int userNotVotedTasksCount
) {
    public static UserTeamView of(final DBUserTeam userTeam, final DBUser user, final int userNotVotedTasksCount) {
        return new UserTeamView(
                userTeam.teamUuid(),
                userTeam.teamName(),
                UserPublicView.of(user),
                userTeam.membershipStatus(),
                userNotVotedTasksCount
        );
    }
}
