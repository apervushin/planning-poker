package in.pervush.poker.service.push;

import in.pervush.poker.model.events.TaskCreatedEvent;
import in.pervush.poker.repository.PushTokensRepository;
import in.pervush.poker.service.UserTeamSettingsService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PushNotificationsService {

    private static final int MAX_USER_DEVICES_TO_SEND_PUSH = 5;
    private static final String TASK_CREATED_ROUTE_TEMPLATE = "task?teamId=%s&taskId=%s";

    private final PushTokensRepository pushTokensRepository;
    private final ApnsService apnsService;
    private final UserTeamSettingsService userTeamSettingsService;

    public PushNotificationsService(PushTokensRepository pushTokensRepository, ApnsService apnsService,
                                    UserTeamSettingsService userTeamSettingsService) {
        this.pushTokensRepository = pushTokensRepository;
        this.apnsService = apnsService;
        this.userTeamSettingsService = userTeamSettingsService;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void setPushToken(final UUID userUuid, final UUID deviceUuid, final String token) {
        pushTokensRepository.setPushToken(userUuid, deviceUuid, token);
    }

    @Async
    @EventListener
    public void handleTaskCreated(final TaskCreatedEvent event) {
        final var usersUuids = event.teamUsersNotVotedTasksCount().keySet();
        final var tokens = pushTokensRepository.getTokens(usersUuids, MAX_USER_DEVICES_TO_SEND_PUSH);

        for (final var token : tokens) {
            if (token.userUuid().equals(event.userUuid())) {
                continue;
            }
            if (!userTeamSettingsService.isNewTasksPushNotificationsEnabled(event.teamUuid(), token.userUuid())) {
                continue;
            }
            apnsService.sendPush(
                    token.token(),
                    event.teamName(),
                    event.taskName(),
                    event.teamUsersNotVotedTasksCount().getOrDefault(token.userUuid(), 0),
                    String.format(TASK_CREATED_ROUTE_TEMPLATE, event.teamUuid().toString(), event.taskUuid().toString())
            );
        }
    }
}
