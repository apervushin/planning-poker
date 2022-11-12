package in.pervush.poker.model.tasks;

import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserPublicView;
import in.pervush.poker.model.votes.DBUserVoteStat;

import java.util.ArrayList;
import java.util.List;

public record TeamTasksStatView(int totalTasksCount, List<UserVotesStatView> userVotesStat) {

    public static TeamTasksStatView of(final List<DBUserVoteStat> usersVotesStat, final List<DBUser> users,
                                       final int totalTasksCount) {
        final List<UserVotesStatView> usersVotesStatView = new ArrayList<>();
        for (int i = 0; i < usersVotesStat.size(); ++i) {
            usersVotesStatView.add(new UserVotesStatView(
                    UserPublicView.of(users.get(i)),
                    usersVotesStat.get(i).votedTasksCount())
            );
        }
        return new TeamTasksStatView(totalTasksCount, usersVotesStatView);
    }
}
