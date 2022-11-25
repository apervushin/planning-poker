package in.pervush.poker.service;

import in.pervush.poker.model.events.TaskCreatedEvent;
import in.pervush.poker.repository.PushTokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushNotificationsService {

    private static final int MAX_USER_DEVICES_TO_SEND_PUSH = 5;
    private static final String TASK_CREATED_ROUTE_TEMPLATE = "task?teamId=%s&taskId=%s";

    private final PushTokensRepository pushTokensRepository;
    private final ApnsService apnsService;

    public void setPushToken(final UUID userUuid, final UUID deviceUuid, final String token) {
        pushTokensRepository.setPushToken(userUuid, deviceUuid, token);
    }

    @Async
    @EventListener
    public void handleTaskCreated(final TaskCreatedEvent event) {
        final var usersUuids = event.teamUsersNotVotedTasksCount().keySet();
        usersUuids.remove(event.userUuid());
        final var tokens = pushTokensRepository.getTokens(usersUuids, MAX_USER_DEVICES_TO_SEND_PUSH);

        for (final var token : tokens) {
            apnsService.sendPush(
                    token.token(),
                    event.taskName(),
                    event.teamUsersNotVotedTasksCount().getOrDefault(token.userUuid(), 0),
                    String.format(TASK_CREATED_ROUTE_TEMPLATE, event.teamUuid().toString(), event.taskUuid().toString())
            );
        }
    }
}
