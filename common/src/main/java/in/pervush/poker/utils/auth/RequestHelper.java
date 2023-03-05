package in.pervush.poker.utils.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.InvalidJwtTokenException;
import in.pervush.poker.utils.InstantUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestHelper {

    public static final String SESSION_COOKIE_NAME = "SESSIONID";
    public static final String DEVICE_UUID_HEADER_NAME = "x-device-uuid";
    private static final String JWT_ISSUER = "planning-poker";
    private final AuthenticationProperties authenticationProperties;

    public RequestHelper(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    public String getAuthToken(final UUID userUuid) {
        final var now = InstantUtils.now();
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(authenticationProperties.cookie().ttl()))
                .withSubject(userUuid.toString())
                .sign(Algorithm.HMAC512(authenticationProperties.jwtSecret()));
    }

    UUID getUserUuid(final String token) throws InvalidJwtTokenException {
        try {
            final var require = JWT.require(Algorithm.HMAC512(authenticationProperties.jwtSecret()))
                    .build();
            final var verify = require.verify(token);
            return UUID.fromString(verify.getSubject());
        } catch (final RuntimeException ex) {
            throw new InvalidJwtTokenException(token, ex);
        }
    }

}
