package in.pervush.poker.model;

import java.util.UUID;

public record DBUserTeamSettings(UUID teamUuid, UUID userUuid, boolean notificationsEnabled) {
}
