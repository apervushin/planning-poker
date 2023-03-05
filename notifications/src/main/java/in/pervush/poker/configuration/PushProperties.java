package in.pervush.poker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "push")
public record PushProperties(ApnsProperties apns) {

    public record ApnsProperties (String teamId,
                                  String keyId,
                                  String p8KeyPath,
                                  String bundle) {
    }
}
