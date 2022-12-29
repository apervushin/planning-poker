package in.pervush.poker.model.tasks;

import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserPublicView;
import in.pervush.poker.model.votes.DBUserVoteStat;

public record UserVotesStatView(UserPublicView user, int votedTasksCount) {

    public static UserVotesStatView of(final DBUser user, final DBUserVoteStat userVotesStat) {
        return new UserVotesStatView(UserPublicView.of(user), userVotesStat.votedTasksCount());
    }
}
