package in.pervush.poker.model.user;

import java.time.Instant;
import java.util.UUID;

public record DBUser(UUID userUuid, String email, String name, Instant createDtm) {
}
