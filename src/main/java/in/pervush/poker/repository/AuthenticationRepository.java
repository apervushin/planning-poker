package in.pervush.poker.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.TokenNotExistsException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.util.UUID;

@Repository
public class AuthenticationRepository {

    private final Cache<String, UUID> tokensCache;

    public AuthenticationRepository(final AuthenticationProperties properties) {
        tokensCache = CacheBuilder.newBuilder().expireAfterAccess(properties.getCookie().getTtl()).build();
    }

    public UUID getUserUuid(final String token) {
        final var userUuid = tokensCache.getIfPresent(token);
        if (userUuid == null) {
            throw new TokenNotExistsException();
        }
        return userUuid;
    }

    public String createToken(final UUID userUuid) {
        final var token = generateToken();
        tokensCache.put(token, userUuid);
        return token;
    }

    private static String generateToken() {
        return RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());
    }
}
