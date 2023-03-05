package in.pervush.poker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "auth")
public record AuthenticationProperties(
        Cookie cookie,
        String jwtSecret
) {

    public record Cookie (
            Duration ttl,
            boolean ssl
    ) {
        public Cookie() {
            this(Duration.ofDays(31), true);
        }
    }
}
