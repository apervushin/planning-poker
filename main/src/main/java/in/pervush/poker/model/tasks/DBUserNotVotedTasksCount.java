package in.pervush.poker.model.tasks;

import java.util.UUID;

public record DBUserNotVotedTasksCount(UUID userUuid, int notVotedTasksCount) {
}
