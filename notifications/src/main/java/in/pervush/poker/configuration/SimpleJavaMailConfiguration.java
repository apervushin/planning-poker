package in.pervush.poker.configuration;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleJavaMailConfiguration {

    @Bean
    public Mailer mailer(@Value("${smtp.host}") final String host, @Value("${smtp.port}") final int port) {
        return MailerBuilder.withSMTPServer(host, port).buildMailer();
    }
}
