package in.pervush.poker.service.email;

import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmtpService {

    private final Mailer mailer;
    private final String fromEmail;
    private final String fromName;

    public SmtpService(Mailer mailer,
                       @Value("${smtp.from.email}") String fromEmail,
                       @Value("${smtp.from.name}") String fromName) {
        this.mailer = mailer;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void sendEmail(final String email, final String name, final String subject, final String body) {
        final var payload = EmailBuilder.startingBlank()
                .from(fromName, fromEmail)
                .to(name, email)
                .withSubject(subject)
                .withPlainText(body)
                .buildEmail();
        mailer.sendMail(payload);
    }
}
