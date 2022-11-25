package in.pervush.poker.repository;

import in.pervush.poker.model.notifications.push.DBPushToken;
import in.pervush.poker.repository.postgres.PushTokensMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PushTokensRepository {

    private final PushTokensMapper mapper;

    public void setPushToken(final UUID userUuid, final UUID deviceUuid, final String token) {
        mapper.deletePushToken(token);
        mapper.setPushToken(userUuid, deviceUuid, token, InstantUtils.now());
    }

    public List<DBPushToken> getTokens(final Set<UUID> usersUuids, final int limitPerUser) {
        return mapper.getTokens(usersUuids, limitPerUser);
    }
}
