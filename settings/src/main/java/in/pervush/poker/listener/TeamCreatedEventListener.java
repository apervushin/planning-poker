package in.pervush.poker.listener;

import in.pervush.poker.model.events.TeamCreatedEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class TeamCreatedEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleTeamCreatedEvent(final TeamCreatedEvent event) {
        userTeamSettingsService.createUser(event.teamUuid(), event.userUuid());
    }

}
