package in.pervush.poker.service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import in.pervush.poker.configuration.PushProperties;
import liquibase.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
@Slf4j
public class ApnsService {

    private static final String ROUTE_PROPERTY_NAME = "route";
    private final ApnsClient apnsClient;
    private final String bundle;

    public ApnsService(final ApnsClient apnsClient, final PushProperties properties) {
        this.apnsClient = apnsClient;
        this.bundle = properties.getApns().getBundle();
    }

    public void sendPush(final String token, final String message, final int badgeCount,
                         @Nullable final String route) {
        log.info("Send push. Token: {}, message: {}, badgeCount: {}", token, message, badgeCount);
        final var payload = new SimpleApnsPayloadBuilder()
                .setAlertBody(message)
                .setBadgeNumber(badgeCount);
        if (!StringUtil.isEmpty(route)) {
            payload.addCustomProperty(ROUTE_PROPERTY_NAME, route);
        }

        final var notification = new SimpleApnsPushNotification(token, bundle, payload.build());

        final var future
                = apnsClient.sendNotification(notification);

        future.whenComplete((response, cause) -> {
            if (response == null) {
                log.error("Error sending push.", cause);
                return;
            }
            final var notification1 = response.getPushNotification();
            if (!response.isAccepted()) {
                log.error("Push push was not accepted by apns gateway. {}", notification1);
                return;
            }
            final var reason = response.getRejectionReason();
            if (reason.isEmpty()) {
                log.debug("Successfully sent push {}", notification1);
                return;
            }
            log.warn("Push {} was not send. Reason: {}", notification1, reason.get());
        });
    }
}
