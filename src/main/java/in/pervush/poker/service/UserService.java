package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    public final static int USER_NAME_NAME_MAX_LENGTH = 50;

    private final UsersRepository adapter;

    public DBUser createUser(final String name) {
        final String nameTrimmed = Strings.trimToNull(name);
        validateUserName(nameTrimmed);
        final var userUuid = UUID.randomUUID();
        return adapter.createUser(userUuid, nameTrimmed);
    }

    public DBUser getUser(final UUID userUuid) {
        return adapter.getUser(userUuid);
    }

    private static void validateUserName(final String name) {
        if (Strings.isBlank(name) || name.length() > USER_NAME_NAME_MAX_LENGTH) {
            throw new ErrorStatusException(ErrorStatus.INVALID_USER_NAME);
        }
    }
}
