package in.pervush.poker.service.email;

import in.pervush.poker.model.events.ConfirmationCodeRequestedEvent;
import in.pervush.poker.model.events.UserCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailNotificationsService.class);

    private static final String WELCOME_SUBJECT = "Registration";
    private static final String WELCOME_BODY = "Congratulations! You successfully registered on Estimate.";
    private static final String OTP_SUBJECT = "Your confirmation code";
    private static final String OTP_BODY = "Your Estimate confirmation code is %s";
    private final SmtpService service;

    public EmailNotificationsService(SmtpService service) {
        this.service = service;
    }

    @Async
    @EventListener
    public void handleUserCreatedEvent(final UserCreatedEvent event) {
        log.debug("handleUserCreatedEvent. Event: {}", event);
        service.sendEmail(event.email(), event.name(), WELCOME_SUBJECT, WELCOME_BODY);
    }

    @Async
    @EventListener
    public void handleConfirmationCodeRequestedEvent(final ConfirmationCodeRequestedEvent event) {
        log.debug("handleConfirmationCodeRequestedEvent. Event: {}", event);
        final var body = String.format(OTP_BODY, event.confirmationCode());
        service.sendEmail(event.email(), null, OTP_SUBJECT, body);
    }
}
