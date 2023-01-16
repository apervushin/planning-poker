package in.pervush.poker.service;

import in.pervush.poker.configuration.SimpleJavaMailConfiguration;
import in.pervush.poker.service.email.SmtpService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(SmtpService.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import(SimpleJavaMailConfiguration.class)
public class SmtpServiceTests {

    @Autowired
    private SmtpService service;

    @Test
    @Disabled("Only manual usage")
    void sendEmail_success() {
        service.sendEmail("alex@pervush.in", null, "Test estimate mail", "Hello, world!");
    }
}
