package in.pervush.poker.utils.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.InvalidJwtTokenException;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final HttpServletRequest request;

    public void setAuthCookie(final UserDetailsImpl user) {
        final var cookie = new Cookie(SESSION_COOKIE_NAME, buildToken(user));
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

    private String buildToken(final UserDetailsImpl user) {
        final var now = InstantUtils.now();
        return JWT.create()
                .withIssuer(JWT_ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(now.plus(authenticationProperties.getCookie().getTtl()))
                .withSubject(user.getUserUuid().toString())
                .sign(Algorithm.HMAC512(authenticationProperties.getJwtSecret()));
    }
}
