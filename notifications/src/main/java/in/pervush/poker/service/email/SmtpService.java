package in.pervush.poker.service.email;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmtpService {

    private final Mailer mailer;
    private final String from;

    public SmtpService(Mailer mailer, @Value("${smtp.from}") String from) {
        this.mailer = mailer;
        this.from = from;
    }

    public void sendEmail(final String email, final String name, final String subject, final String body) {
        final var payload = EmailBuilder.startingBlank()
                .from(from)
                .to(name, email)
                .withSubject(subject)
                .withPlainText(body)
                .buildEmail();
        mailer.sendMail(payload);
    }
}
