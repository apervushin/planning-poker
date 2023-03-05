package in.pervush.poker.listener;

import in.pervush.poker.model.events.TeamDeletedEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TeamDeletedEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    public TeamDeletedEventListener(UserTeamSettingsService userTeamSettingsService) {
        this.userTeamSettingsService = userTeamSettingsService;
    }

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTeamDeletedEvent(final TeamDeletedEvent event) {
        userTeamSettingsService.deleteTeam(event.teamUuid());
    }
}
