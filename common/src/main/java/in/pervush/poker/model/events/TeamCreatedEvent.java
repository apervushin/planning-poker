package in.pervush.poker.model.events;

import java.util.UUID;

public record TeamCreatedEvent(UUID teamUuid, String teamName, UUID userUuid) {
}
