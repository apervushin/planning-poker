package in.pervush.poker.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "auth")
@Getter
public class AuthenticationProperties {

    private final Duration cookieTtl = Duration.ofDays(31);
}
