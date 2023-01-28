package in.pervush.poker.model.events;

import java.util.UUID;

public record UserLeftTeamEvent(UUID teamUuid, UUID userUuid) {
}
