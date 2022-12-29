package in.pervush.poker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "push")
@Data
public class PushProperties {

    private ApnsProperties apns;

    @Data
    public static class ApnsProperties {
        private String teamId;
        private String keyId;
        private String p8KeyPath;
        private String bundle;
    }
}
