package in.pervush.poker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("settings")
@Data
public class SettingsProperties {

    private UserTeamSettings userTeam = new UserTeamSettings();

    @Data
    public static class UserTeamSettings {
        private boolean notificationsEnabled = true;
    }
}
