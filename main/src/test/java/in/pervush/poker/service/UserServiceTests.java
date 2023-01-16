package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.tests.TestPostgresConfiguration;
import in.pervush.poker.exception.InvalidConfirmationCodeException;
import in.pervush.poker.exception.InvalidEmailException;
import in.pervush.poker.exception.InvalidStepException;
import in.pervush.poker.exception.InvalidUserNameException;
import in.pervush.poker.exception.TooManyConfirmationAttemptsException;
import in.pervush.poker.exception.TooWeakPasswordException;
import in.pervush.poker.model.events.ConfirmationCodeRequestedEvent;
import in.pervush.poker.model.events.UserCreatedEvent;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig({UsersRepository.class, UserService.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class,
        UserServiceTests.MockitoPublisherConfiguration.class})
@Transactional
public class UserServiceTests {

    @TestConfiguration
    public static class MockitoPublisherConfiguration {

        @Bean
        @Primary
        ApplicationEventPublisher publisher() {
            return mock(ApplicationEventPublisher.class);
        }
    }

    @Autowired
    private UserService service;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<ConfirmationCodeRequestedEvent> confirmationCodeRequestedEventCaptor;

    @Captor
    private ArgumentCaptor<UserCreatedEvent> userCreatedEventArgumentCaptor;

    @BeforeEach
    void resetMocks() {
        reset(eventPublisher);
    }

    @Test
    void createAndGetUser_success() {
        final var expected = service.createUser("test@example.com", "Passw0rd!","Test User");
        final var actual = service.getUser(expected.userUuid());
        assertEquals(expected, actual);
    }

    @Test
    void createAndGetUser_weakPassword_ErrorStatusException() {
        assertThrows(TooWeakPasswordException.class, () -> service.createUser(
                "test@example.com",
                "abc",
                "Test User"
        ));
    }

    @Test
    void createAndGetUser_localhostEmail_ErrorStatusException() {
        assertThrows(InvalidEmailException.class, () -> service.createUser(
                "test@localhost",
                "abc",
                "Test User"
        ));
    }

    @Test
    void createAndGetUser_invalidEmail_ErrorStatusException() {
        assertThrows(InvalidEmailException.class, () -> service.createUser(
                "test_example.com",
                "Passw0rd!",
                "Test User"
        ));
    }

    @Test
    void createUser_tooLongUserName_ErrorStatusException() {
        assertThrows(InvalidUserNameException.class, () -> service.createUser(
                "test@localhost",
                "abc",
                RandomStringUtils.random(UserService.USER_NAME_NAME_MAX_LENGTH + 1)
        ));
    }

    @Test
    void registration_success() {
        final var email = "User@Example.Com";
        final var emailLower = email.toLowerCase();
        final var deviceUuid = UUID.randomUUID();
        final var password = "Passw0rd!";
        final var name = "User name";

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        final var actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        assertThat(actualEvent).usingRecursiveComparison().ignoringFields("confirmationCode")
                .isEqualTo(new ConfirmationCodeRequestedEvent(emailLower, ""));

        final var userStep2 = service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        assertThat(userStep2).isEmpty();

        final var user = service.loginStep3(name, deviceUuid);
        assertEquals(emailLower, user.email());
        verify(eventPublisher, times(2)).publishEvent(userCreatedEventArgumentCaptor.capture());
        assertThat(userCreatedEventArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("userUuid", "createDtm")
                .isEqualTo(new UserCreatedEvent(null, emailLower, name, null));
    }

    @Test
    void login_success() {
        final var email = "User@Example.Com";
        final var emailLower = email.toLowerCase();
        final var deviceUuid = UUID.randomUUID();

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        var actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        service.loginStep3("Test User", deviceUuid);

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher, times(3)).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        final var user = service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        assertThat(user.get().email()).isEqualTo(emailLower);
    }

    @Test
    void loginStep1_invalidEmail_InvalidEmailException() {
        final var email = "user_example.com";

        assertThrows(InvalidEmailException.class, () -> service.loginStep1(email, UUID.randomUUID()));
    }

    @Test
    void loginStep1_localhostEmail_InvalidEmailException() {
        final var email = "user@localhost";

        assertThrows(InvalidEmailException.class, () -> service.loginStep1(email, UUID.randomUUID()));
    }

    @Test
    void loginStep2_invalidConfirmationCode_InvalidConfirmationCodeExceptionAndTooManyConfirmationAttemptsException() {
        final var deviceUuid = UUID.randomUUID();
        service.loginStep1("user@example.com", deviceUuid);
        for (int i = 0; i < UserService.MAX_CONFIRMATION_ATTEMPTS; ++i) {
            assertThrows(InvalidConfirmationCodeException.class, () ->
                    service.loginStep2("", deviceUuid));
        }
        assertThrows(TooManyConfirmationAttemptsException.class, () ->
                service.loginStep2("", deviceUuid));
    }

    @Test
    void loginStep2_step2WithoutStep1_InvalidStepException() {
        assertThrows(InvalidStepException.class, () -> service.loginStep2("", UUID.randomUUID()));
    }

    @Test
    void loginStep2_step2AfterStep2_InvalidStepException() {
        final var email = "User@Example.Com";
        final var deviceUuid = UUID.randomUUID();

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        var actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        assertThrows(InvalidStepException.class, () -> service.loginStep2(actualEvent.confirmationCode(), deviceUuid));
    }

    @Test
    void loginStep3_existingUser_InvalidStepException() {
        final var email = "User@Example.Com";
        final var deviceUuid = UUID.randomUUID();

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        var actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        service.loginStep3("Test User", deviceUuid);

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher, times(3)).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        service.loginStep2(actualEvent.confirmationCode(), deviceUuid);

        assertThrows(InvalidStepException.class, () -> service.loginStep3("Test User", deviceUuid));
    }

    @Test
    void loginStep3_newUserNullName_InvalidStepException() {
        final var email = "User@Example.Com";
        final var deviceUuid = UUID.randomUUID();

        service.loginStep1(email, deviceUuid);
        verify(eventPublisher).publishEvent(confirmationCodeRequestedEventCaptor.capture());
        var actualEvent = confirmationCodeRequestedEventCaptor.getValue();
        service.loginStep2(actualEvent.confirmationCode(), deviceUuid);
        assertThrows(InvalidUserNameException.class, () -> service.loginStep3(null, deviceUuid));
    }
}
