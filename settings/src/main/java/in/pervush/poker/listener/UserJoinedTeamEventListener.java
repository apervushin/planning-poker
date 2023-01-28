package in.pervush.poker.listener;

import in.pervush.poker.model.events.UserJoinedTeamEvent;
import in.pervush.poker.service.UserTeamSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserJoinedTeamEventListener {

    private final UserTeamSettingsService userTeamSettingsService;

    @TransactionalEventListener(fallbackExecution = true, phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserJoinedTeamEvent(final UserJoinedTeamEvent event) {
        userTeamSettingsService.createUser(event.teamUuid(), event.userUuid());
    }
}
