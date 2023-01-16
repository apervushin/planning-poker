package in.pervush.poker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smtp")
@Data
public class SmtpProperties {

    private int port = 25;
    private String host = "localhost";

}
