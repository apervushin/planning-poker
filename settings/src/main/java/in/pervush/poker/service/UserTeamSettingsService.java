package in.pervush.poker.service;

import in.pervush.poker.configuration.SettingsProperties;
import in.pervush.poker.exception.SettingsNotFoundException;
import in.pervush.poker.model.DBUserTeamSettings;
import in.pervush.poker.repository.UserTeamSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserTeamSettingsService {

    private final SettingsProperties.UserTeamSettings userTeamDefaultSettings;
    private final UserTeamSettingsRepository repository;

    public UserTeamSettingsService(final SettingsProperties userTeamDefaultSettings,
                                   final UserTeamSettingsRepository repository) {
        this.repository = repository;
        this.userTeamDefaultSettings = userTeamDefaultSettings.getUserTeam();
    }

    public void createUser(final UUID teamUuid, final UUID userUuid) throws SettingsNotFoundException {
        final var defaultSettings =
                new DBUserTeamSettings(teamUuid, userUuid, userTeamDefaultSettings.isNotificationsEnabled());
        repository.createUser(defaultSettings);
    }

    public void deleteTeam(final UUID teamUuid) throws SettingsNotFoundException {
        repository.deleteTeam(teamUuid);

    }

    public void deleteUser(final UUID teamUuid, final UUID userUuid) throws SettingsNotFoundException {
        repository.deleteUser(teamUuid, userUuid);
    }

    public DBUserTeamSettings getUserTeamSettings(final UUID teamUuid, final UUID userUuid)
            throws SettingsNotFoundException {
        return repository.getUserTeamSettings(teamUuid, userUuid);
    }

    @Transactional
    public void setUserTeamSettings(final UUID teamUuid, final UUID userUuid,
                                    final boolean newTasksPushNotificationsEnabled) throws SettingsNotFoundException {
        repository.setUserTeamSettings(teamUuid, userUuid, newTasksPushNotificationsEnabled);
    }

    public boolean isNewTasksPushNotificationsEnabled(final UUID teamUuid, final UUID userUuid) {
        return repository.getUserTeamSettings(teamUuid, userUuid).notificationsEnabled();
    }
}
