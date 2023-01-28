package in.pervush.poker.listener;

import in.pervush.poker.model.events.TeamDeletedEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TeamDeletedEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTeamDeletedEvent(final TeamDeletedEvent event) {
        userTeamSettingsService.deleteTeam(event.teamUuid());
    }
}
