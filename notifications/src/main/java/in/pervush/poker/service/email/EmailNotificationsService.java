package in.pervush.poker.service.email;

import in.pervush.poker.model.events.ConfirmationCodeRequestedEvent;
import in.pervush.poker.model.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationsService {

    private static final String WELCOME_SUBJECT = "Registration";
    private static final String WELCOME_BODY = "Congratulations! You successfully registered on Estimate.";
    private static final String OTP_SUBJECT = "Your confirmation code";
    private static final String OTP_BODY = "Your Estimate confirmation code is %s";
    private final SmtpService service;

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
