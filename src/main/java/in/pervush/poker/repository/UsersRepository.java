package in.pervush.poker.repository;

import in.pervush.poker.exception.UnauthorizedException;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.postgres.UsersMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UsersRepository {

    private final UsersMapper mapper;

    public DBUser createUser(final UUID userUuid, final String name) {
        final var createDtm = InstantUtils.now();
        mapper.createUser(userUuid, name, createDtm);
        return new DBUser(userUuid, name, createDtm);
    }

    public DBUser getUser(final UUID userUuid) {
        return mapper.getUser(userUuid).orElseThrow(UnauthorizedException::new);
    }
}
