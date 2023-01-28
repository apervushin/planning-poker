package in.pervush.poker.repository;

import in.pervush.poker.exception.SettingsNotFoundException;
import in.pervush.poker.model.DBUserTeamSettings;
import in.pervush.poker.repository.postgres.UserTeamSettingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserTeamSettingsRepository {

    private final UserTeamSettingsMapper mapper;

    public void createUser(final DBUserTeamSettings settings) {
        mapper.createUser(settings);
    }

    public void deleteTeam(final UUID teamUuid) throws SettingsNotFoundException {
        final boolean deleted = mapper.deleteTeam(teamUuid);
        if (!deleted) {
            throw new SettingsNotFoundException();
        }
    }

    public void deleteUser(final UUID teamUuid, final UUID userUuid) throws SettingsNotFoundException {
        final boolean deleted = mapper.deleteUser(teamUuid, userUuid);
        if (!deleted) {
            throw new SettingsNotFoundException();
        }
    }

    public DBUserTeamSettings getUserTeamSettings(final UUID teamUuid, final UUID userUuid)
            throws SettingsNotFoundException {
        return mapper.getUserTeamSettings(teamUuid, userUuid).orElseThrow(SettingsNotFoundException::new);
    }

    public void setUserTeamSettings(final UUID teamUuid, final UUID userUuid,
                                    final boolean newTasksPushNotificationsEnabled) throws SettingsNotFoundException {
        final boolean updated = mapper.setUserTeamSettings(teamUuid, userUuid, newTasksPushNotificationsEnabled);
        if (!updated) {
            throw new SettingsNotFoundException();
        }
    }
}
