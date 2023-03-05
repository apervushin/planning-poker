package in.pervush.poker.listener;

import in.pervush.poker.model.events.TeamCreatedEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TeamCreatedEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    public TeamCreatedEventListener(UserTeamSettingsService userTeamSettingsService) {
        this.userTeamSettingsService = userTeamSettingsService;
    }

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTeamCreatedEvent(final TeamCreatedEvent event) {
        userTeamSettingsService.createUser(event.teamUuid(), event.userUuid());
    }

}
