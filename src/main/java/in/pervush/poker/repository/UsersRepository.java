package in.pervush.poker.repository;

import in.pervush.poker.exception.EmailExistsException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.postgres.UsersMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsersRepository {

    private final UsersMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public DBUser createUser(final String email, final String password, final String name) {
        final var userUuid = UUID.randomUUID();
        final var createDtm = InstantUtils.now();
        final var passwordEncoded = passwordEncoder.encode(password);
        try {
            mapper.createUser(userUuid, email, passwordEncoded, name, createDtm);
        } catch (DuplicateKeyException ex) {
            throw new EmailExistsException();
        }
        return new DBUser(userUuid, email, passwordEncoded, name, createDtm);
    }

    public DBUser getUser(final UUID userUuid) {
        return mapper.getUser(userUuid).orElseThrow(UserNotFoundException::new);
    }

    public DBUser getUser(final String email, final String password) {
        final var dbUser = mapper.getUserByEmail(email).orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(password, dbUser.passwordEncoded())) {
            throw new UserNotFoundException();
        }
        return dbUser;
    }
}
