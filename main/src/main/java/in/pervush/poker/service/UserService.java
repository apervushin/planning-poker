package in.pervush.poker.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import in.pervush.poker.exception.EmailExistsException;
import in.pervush.poker.exception.InvalidConfirmationCodeException;
import in.pervush.poker.exception.InvalidEmailException;
import in.pervush.poker.exception.InvalidStepException;
import in.pervush.poker.exception.InvalidUserNameException;
import in.pervush.poker.exception.TooManyConfirmationAttemptsException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.events.ConfirmationCodeRequestedEvent;
import in.pervush.poker.model.events.UserCreatedEvent;
import in.pervush.poker.model.login.LoginState;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserService {

    public static final int USER_NAME_NAME_MAX_LENGTH = 50;
    public static final int MAX_CONFIRMATION_ATTEMPTS = 3;
    private static final int USER_EMAIL_MAX_LENGTH = 50;
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    private final UsersRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    private final Cache<UUID, LoginState> loginAttemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15)).build();

    public UserService(UsersRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public DBUser getUser(final UUID userUuid) {
        return repository.getUser(userUuid);
    }

    public void loginStep1(final String email, final UUID deviceUuid) throws InvalidEmailException {
        final String emailLower = Strings.toRootLowerCase(email);
        validateEmail(emailLower);
        final var confirmationCode = generateConfirmationCode();
        loginAttemptsCache.put(
                deviceUuid,
                new LoginState(emailLower, new AtomicBoolean(), confirmationCode, new AtomicInteger(0))
        );
        eventPublisher.publishEvent(new ConfirmationCodeRequestedEvent(
                emailLower,
                confirmationCode
        ));
    }

    public Optional<DBUser> loginStep2(final String confirmationCode, final UUID deviceUuid)
            throws InvalidStepException, TooManyConfirmationAttemptsException, InvalidConfirmationCodeException {
        final var cacheRecord = loginAttemptsCache.getIfPresent(deviceUuid);
        if (cacheRecord == null) {
            throw new InvalidStepException();
        }
        if (cacheRecord.attemptsCount().incrementAndGet() > MAX_CONFIRMATION_ATTEMPTS) {
            throw new TooManyConfirmationAttemptsException();
        }
        if (!cacheRecord.confirmationCode().equals(confirmationCode)) {
            throw new InvalidConfirmationCodeException();
        }
        if (cacheRecord.confirmed().compareAndExchangeRelease(false, true)) {
            throw new InvalidStepException();
        }
        try {
            final var user = repository.getUser(cacheRecord.email());
            loginAttemptsCache.invalidate(deviceUuid);
            return Optional.of(user);
        } catch (final UserNotFoundException ex) {
            return Optional.empty();
        }
    }

    public DBUser loginStep3(final String name, final UUID deviceUuid)
            throws InvalidStepException, BadCredentialsException, InvalidUserNameException, EmailExistsException {
        final var cacheRecord = loginAttemptsCache.getIfPresent(deviceUuid);
        if (cacheRecord == null) {
            throw new InvalidStepException();
        }

        validateUserName(name);
        final String nameTrimmed = Strings.trimToNull(name);
        final var user = repository.createUser(cacheRecord.email(), nameTrimmed);

        eventPublisher.publishEvent(new UserCreatedEvent(
                user.userUuid(),
                user.email(),
                user.name(),
                user.createDtm()
        ));

        loginAttemptsCache.invalidate(deviceUuid);

        return user;
    }

    private static void validateUserName(final String name) throws InvalidUserNameException {
        if (Strings.isBlank(name) || name.length() > USER_NAME_NAME_MAX_LENGTH) {
            throw new InvalidUserNameException();
        }
    }

    private static void validateEmail(final String email) throws InvalidEmailException {
        if (!EMAIL_VALIDATOR.isValid(email) || email.length() > USER_EMAIL_MAX_LENGTH) {
            throw new InvalidEmailException();
        }
    }

    private static String generateConfirmationCode() {
        return RandomStringUtils.random(6, false, true);
    }
}
