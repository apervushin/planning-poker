package in.pervush.poker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("settings")
public record SettingsProperties(
        UserTeamSettings userTeam
) {
    public SettingsProperties() {
        this(new UserTeamSettings());
    }

    public record UserTeamSettings(boolean notificationsEnabled) {
        public UserTeamSettings() {
            this(false);
        }
    }
}
