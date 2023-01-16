package in.pervush.poker.utils.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.InvalidJwtTokenException;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestHelper {

    public static final String SESSION_COOKIE_NAME = "SESSIONID";
    public static final String DEVICE_UUID_HEADER_NAME = "x-device-uuid";
    private static final String SESSION_COOKIE_PATH = "/api/";
    private static final String JWT_ISSUER = "planning-poker";
    private final AuthenticationProperties authenticationProperties;
    private final HttpServletResponse response;

    public void setAuthCookie(final UUID userUuid) {
        final var cookie = new Cookie(SESSION_COOKIE_NAME, buildToken(userUuid));
        cookie.setSecure(authenticationProperties.getCookie().isSsl());
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int)authenticationProperties.getCookie().getTtl().toSeconds());
        cookie.setPath(SESSION_COOKIE_PATH);
        response.addCookie(cookie);
    }

    UUID getUserUuid(final String token) throws InvalidJwtTokenException {
        try {
            final var require = JWT.require(Algorithm.HMAC512(authenticationProperties.getJwtSecret()))
                    .build();
            final var verify = require.verify(token);
            return UUID.fromString(verify.getSubject());
        } catch (Exception ex) {
            throw new InvalidJwtTokenException(token, ex);
        }
    }

    private String buildToken(final UUID userUuid) {
        final var now = InstantUtils.now();
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(authenticationProperties.getCookie().getTtl()))
                .withSubject(userUuid.toString())
                .sign(Algorithm.HMAC512(authenticationProperties.getJwtSecret()));
    }
}