package in.pervush.poker.service;

import in.pervush.poker.configuration.TestPostgresConfiguration;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.RandomStringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig({UsersRepository.class, UserService.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.yml")
@Import(TestPostgresConfiguration.class)
@Transactional
public class UserServiceTests {

    @Autowired
    private UserService service;

    @Test
    void createAndGetUser_success() {
        final var expected = service.createUser("Test User");
        final var actual = service.getUser(expected.userUuid());
        assertEquals(expected, actual);
    }

    @Test
    void createUser_tooLongUserName_ErrorStatusException() {
        final var ex = assertThrows(ErrorStatusException.class,
                () -> service.createUser(RandomStringUtils.random(UserService.USER_NAME_NAME_MAX_LENGTH + 1)));
        assertEquals(ErrorStatus.INVALID_USER_NAME, ex.getStatus());
    }
}
