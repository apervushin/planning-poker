package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.model.notifications.push.DBPushToken;
import in.pervush.poker.repository.PushTokensRepository;
import in.pervush.poker.repository.TeamsRepository;
import in.pervush.poker.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({PushNotificationsService.class, PushTokensRepository.class, UsersRepository.class,
        TeamsRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@MockBeans({
        @MockBean(ApnsService.class)
})
@Transactional
@EnableAsync
public class PushNotificationsServiceTests {

    @Autowired
    private PushNotificationsService service;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PushTokensRepository pushTokensRepository;

    private UUID userUuid;

    @BeforeEach
    void init() {
        this.userUuid = usersRepository.createUser("email@example.com", "test", "Test user")
                .userUuid();
    }

    @Test
    void setPushToken_twiceForOneDevice_success() {
        final var deviceUuid = UUID.randomUUID();
        final var expected = "token2";
        service.setPushToken(userUuid, deviceUuid, "token1");
        service.setPushToken(userUuid, deviceUuid, expected);

        final var tokens = pushTokensRepository.getTokens(Set.of(userUuid), 10).stream()
                .map(DBPushToken::token).toList();

        assertThat(tokens).containsExactly(expected);
    }

    @Test
    void setPushToken_limitDevicesPerUser_success() {
        final var expected2 = "token2";
        final var expected3 = "token3";
        service.setPushToken(userUuid, UUID.randomUUID(), "token1");
        service.setPushToken(userUuid, UUID.randomUUID(), expected2);
        service.setPushToken(userUuid, UUID.randomUUID(), expected3);
        final var actual = pushTokensRepository
                .getTokens(Set.of(userUuid), 2).stream().map(DBPushToken::token).toList();
        assertThat(actual).containsExactly(expected3, expected2);
    }
}
