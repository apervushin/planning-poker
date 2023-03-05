package in.pervush.poker.listener;

import in.pervush.poker.model.events.UserLeftTeamEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserLeftTeamEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    public UserLeftTeamEventListener(UserTeamSettingsService userTeamSettingsService) {
        this.userTeamSettingsService = userTeamSettingsService;
    }

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserLeftTeamEvent(final UserLeftTeamEvent event) {
        userTeamSettingsService.deleteUser(event.teamUuid(), event.userUuid());
    }
}
