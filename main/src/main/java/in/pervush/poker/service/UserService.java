package in.pervush.poker.service;

import in.pervush.poker.exception.EmailConfirmationCodeDoesNotExistsException;
import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.events.UserCreatedEvent;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final int USER_NAME_NAME_MAX_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final int USER_EMAIL_MAX_LENGTH = 50;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~$^+=<>]).{" + MIN_PASSWORD_LENGTH + ",30}$");
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    private final UsersRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DBUser createUser(final String email, final String password, final String name) {
        final String nameTrimmed = Strings.trimToNull(name);
        final String emailLower = Strings.toRootLowerCase(email);

        validateUserName(nameTrimmed);
        validateEmail(emailLower);
        validatePassword(password);

        final var user = repository.createUser(emailLower, password, nameTrimmed, UUID.randomUUID());

        eventPublisher.publishEvent(new UserCreatedEvent(
                user.userUuid(),
                user.email(),
                user.name(),
                user.createDtm(),
                user.emailConfirmationCode()
        ));

        return user;
    }

    public DBUser getUser(final UUID userUuid) {
        return repository.getUser(userUuid);
    }

    public void confirmEmail(final UUID confirmationCode) throws EmailConfirmationCodeDoesNotExistsException {
        repository.confirmEmail(confirmationCode);
    }

    private static void validateUserName(final String name) {
        if (Strings.isBlank(name) || name.length() > USER_NAME_NAME_MAX_LENGTH) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_NAME);
        }
    }

    private static void validateEmail(final String email) {
        if (!EMAIL_VALIDATOR.isValid(email) || email.length() > USER_EMAIL_MAX_LENGTH) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_EMAIL);
        }
    }

    private static void validatePassword(final String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ErrorStatusException(ErrorStatus.TOO_WEAK_USER_PASSWORD);
        }
    }
}
