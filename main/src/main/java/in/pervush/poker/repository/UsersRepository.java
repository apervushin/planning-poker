package in.pervush.poker.repository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import in.pervush.poker.exception.EmailExistsException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.postgres.UsersMapper;
import in.pervush.poker.utils.InstantUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class UsersRepository {

    private final UsersMapper mapper;

    private final LoadingCache<UUID, Optional<DBUser>> usersCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.of(10, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public Optional<DBUser> load(final UUID userUuid) {
                    return mapper.getUser(userUuid);
                }
            });

    public UsersRepository(UsersMapper mapper) {
        this.mapper = mapper;
    }

    public DBUser createUser(final String email, final String name) throws EmailExistsException {
        final var userUuid = UUID.randomUUID();
        final var createDtm = InstantUtils.now();
        try {
            mapper.createUser(userUuid, email, name, createDtm);
        } catch (final DuplicateKeyException ex) {
            throw new EmailExistsException();
        }
        return new DBUser(userUuid, email, name, createDtm);
    }

    public DBUser getUser(final UUID userUuid) throws UserNotFoundException {
        try {
            return usersCache.get(userUuid).orElseThrow(UserNotFoundException::new);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public DBUser getUser(final String email) throws UserNotFoundException {
        return mapper.getUserByEmail(email).orElseThrow(UserNotFoundException::new);
    }

}
