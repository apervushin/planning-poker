package in.pervush.poker.model.events;

import java.util.Map;
import java.util.UUID;

public record TaskCreatedEvent(UUID userUuid, UUID taskUuid, UUID teamUuid, String taskName,
                               Map<UUID, Integer> teamUsersNotVotedTasksCount) {

}
