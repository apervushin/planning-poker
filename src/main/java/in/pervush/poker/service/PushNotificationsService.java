package in.pervush.poker.service;

import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.repository.PushTokensRepository;
import in.pervush.poker.repository.TeamsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PushNotificationsService {

    private static final int MAX_USER_DEVICES_TO_SEND_PUSH = 5;

    private final PushTokensRepository pushTokensRepository;
    private final TeamsRepository teamsRepository;
    private final ApnsService apnsService;

    public void setPushToken(final UUID userUuid, final UUID deviceUuid, final String token) {
        pushTokensRepository.setPushToken(userUuid, deviceUuid, token);
    }

    @Async
    @EventListener
    public void handleTaskCreated(final DBTask task) {
        final var usersUuids = teamsRepository.getTeamMembers(task.teamUuid()).stream()
                .map(DBUserTeam::userUuid).filter(u -> !task.userUuid().equals(u)).collect(Collectors.toSet());
        final var tokens = pushTokensRepository.getTokens(usersUuids, MAX_USER_DEVICES_TO_SEND_PUSH);

        for (final var token : tokens) {
            apnsService.sendPush(token.token(), task.name(), 1); // TODO count tasks
        }
    }
}
