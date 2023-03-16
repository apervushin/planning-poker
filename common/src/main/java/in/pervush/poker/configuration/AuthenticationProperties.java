package in.pervush.poker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth")
public record AuthenticationProperties(
        Duration sessionTtl,
        String jwtSecret
) {

}
