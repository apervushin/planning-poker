package in.pervush.poker.model.notifications.push;

import java.time.Instant;
import java.util.UUID;

public record DBPushToken(UUID userUuid, UUID deviceUuid, String token, Instant lastUpdateDtm) {
}
