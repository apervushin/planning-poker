package in.pervush.poker.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Team user settings (for particular team member)")
public record TeamSettingsUserView(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean newTasksPushNotificationsEnabled
) {
    public static TeamSettingsUserView of(final DBUserTeamSettings settings) {
        return new TeamSettingsUserView(settings.notificationsEnabled());
    }
}
