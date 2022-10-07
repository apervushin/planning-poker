package in.pervush.poker.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.TokenNotExistsException;
import in.pervush.poker.model.user.DBUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;

@Repository
public class AuthenticationRepository {

    private final Cache<String, DBUser> tokensCache;

    public AuthenticationRepository(final AuthenticationProperties properties) {
        tokensCache = CacheBuilder.newBuilder().expireAfterAccess(properties.getCookie().getTtl()).build();
    }

    public DBUser getUserUuid(final String token) {
        final var userUuid = tokensCache.getIfPresent(token);
        if (userUuid == null) {
            throw new TokenNotExistsException();
        }
        return userUuid;
    }

    public String createToken(final DBUser user) {
        final var token = generateToken();
        tokensCache.put(token, user);
        return token;
    }

    private static String generateToken() {
        return RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());
    }
}
