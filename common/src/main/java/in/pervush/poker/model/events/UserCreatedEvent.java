package in.pervush.poker.model.events;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(UUID userUuid, String email, String name, Instant createDtm,
                               UUID emailConfirmationCode) {
}
