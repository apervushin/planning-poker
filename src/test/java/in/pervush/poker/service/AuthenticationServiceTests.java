package in.pervush.poker.service;

import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.configuration.PasswordEncoderConfiguration;
import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.repository.AuthenticationRepository;
import in.pervush.poker.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({UsersRepository.class, AuthenticationService.class, AuthenticationRepository.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import({TestPostgresConfiguration.class, PasswordEncoderConfiguration.class, AuthenticationProperties.class})
@Transactional
public class AuthenticationServiceTests {

    @Autowired
    private AuthenticationService service;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    void login_success() {
        final var password = "abc";
        final var expected = usersRepository.createUser("test@example.com", password, "Test");
        assertNotNull(service.login(expected.email(), password));
    }

    @Test
    void login_unknownEmail_userNotFoundException() {
        final var password = "abc";
        usersRepository.createUser("test1@example.com", password, "Test");
        assertThrows(UserNotFoundException.class, () -> service.login("test2@example.com", password));
    }

    @Test
    void login_invalidPassword_userNotFoundException() {
        final var email = "test1@example.com";
        usersRepository.createUser(email, "abc", "Test");
        assertThrows(UserNotFoundException.class, () -> service.login(email, "abcd"));
    }
}
