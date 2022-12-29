package in.pervush.poker.model.votes;

import java.util.UUID;

public record DBUserVoteStat(UUID userUuid, int votedTasksCount) {
}
