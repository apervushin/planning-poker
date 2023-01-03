package in.pervush.poker.service;

import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.tests.TestPostgresConfiguration;
import in.pervush.poker.exception.EmailConfirmationCodeDoesNotExistsException;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({UsersRepository.class, UserService.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class})
@Transactional
public class UserServiceTests {

    @Autowired
    private UserService service;

    @Test
    void createAndGetUser_success() {
        final var expected = service.createUser("test@example.com", "Passw0rd!","Test User");
        final var actual = service.getUser(expected.userUuid());
        assertEquals(expected, actual);
    }

    @Test
    void createAndGetUser_weakPassword_ErrorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> service.createUser(
                "test@example.com",
                "abc",
                "Test User"
        ));
        assertEquals(ErrorStatus.TOO_WEAK_USER_PASSWORD, ex.getStatus());
    }

    @Test
    void createAndGetUser_localhostEmail_ErrorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> service.createUser(
                "test@localhost",
                "abc",
                "Test User"
        ));
        assertEquals(ErrorStatus.INVALID_USER_EMAIL, ex.getStatus());
    }

    @Test
    void createAndGetUser_invalidEmail_ErrorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> service.createUser(
                "test_example.com",
                "Passw0rd!",
                "Test User"
        ));
        assertEquals(ErrorStatus.INVALID_USER_EMAIL, ex.getStatus());
    }

    @Test
    void createUser_tooLongUserName_ErrorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class, () -> service.createUser(
                "test@localhost",
                "abc",
                RandomStringUtils.random(UserService.USER_NAME_NAME_MAX_LENGTH + 1)
        ));
        assertEquals(ErrorStatus.INVALID_USER_NAME, ex.getStatus());
    }

    @Test
    void confirmEmail_success() {
        final var user = service.createUser("test@example.com", "Passw0rd!","Test User");
        assertDoesNotThrow(() -> service.confirmEmail(user.emailConfirmationCode()));
    }

    @Test
    void confirmEmailTwice_EmailConfirmationCodeDoesNotExistsException() {
        final var user = service.createUser("test@example.com", "Passw0rd!","Test User");
        assertDoesNotThrow(() -> service.confirmEmail(user.emailConfirmationCode()));
        assertThrows(EmailConfirmationCodeDoesNotExistsException.class,
                () -> service.confirmEmail(user.emailConfirmationCode()));
    }
}
