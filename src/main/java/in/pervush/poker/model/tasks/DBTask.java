package in.pervush.poker.model.tasks;

import java.time.Instant;
import java.util.UUID;

public record DBTask(UUID taskUuid, UUID userUuid, String name, String url, Scale scale, boolean finished,
                     Instant createDtm, int votesCount, boolean voted) {
}
