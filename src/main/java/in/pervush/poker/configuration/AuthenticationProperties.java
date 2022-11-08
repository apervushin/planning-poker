package in.pervush.poker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Data
public class AuthenticationProperties {

    private Cookie cookie = new Cookie();
    private String jwtSecret;

    @Data
    public static class Cookie {
        private Duration ttl = Duration.ofDays(31);
        private boolean ssl = true;
    }
}
