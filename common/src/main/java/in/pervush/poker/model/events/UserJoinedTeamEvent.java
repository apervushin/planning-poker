package in.pervush.poker.model.events;

import java.util.UUID;

public record UserJoinedTeamEvent(UUID teamUuid, UUID userUuid) {
}
