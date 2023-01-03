package in.pervush.poker.service;

import in.pervush.poker.model.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationsService {

    private static final String NAME_KEY = "name";
    private static final String URL_KEY = "url";
    private static final String CONFIRM_EMAIL_URL_TEMPLATE = "https://estimate.pervush.in/confirmEmail/%s";
    private final SendinblueService service;

    @Async
    @EventListener
    public void handleUserCreated(final UserCreatedEvent event) {
        log.debug("handleUserCreated. Event: {}", event);
        final var params = Map.of(
                NAME_KEY, event.name(),
                URL_KEY, String.format(CONFIRM_EMAIL_URL_TEMPLATE, event.emailConfirmationCode())
        );
        service.sendEmail(event.email(), params, 1);
    }
}
