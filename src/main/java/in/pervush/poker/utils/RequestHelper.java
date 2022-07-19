package in.pervush.poker.utils;

import in.pervush.poker.configuration.AuthenticationProperties;
import in.pervush.poker.exception.TokenNotExistsException;
import in.pervush.poker.exception.UnauthorizedException;
import in.pervush.poker.repository.AuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestHelper {

    public static final String SESSION_COOKIE_NAME = "SESSIONID";
    private static final String SESSION_COOKIE_PATH = "/api/";

    private final AuthenticationRepository authenticationRepository;
    private final AuthenticationProperties authenticationProperties;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public UUID getAuthenticatedUserUuid() {
        final var cookie = WebUtils.getCookie(request, SESSION_COOKIE_NAME);
        if (cookie == null) {
            throw new UnauthorizedException();
        }

        try {
            return authenticationRepository.getUserUuid(cookie.getValue());
        } catch (TokenNotExistsException ex) {
            throw new UnauthorizedException();
        }
    }

    public void setAuthCookie(final String token) {
        final var cookie = new Cookie(SESSION_COOKIE_NAME, token);
        cookie.setSecure(authenticationProperties.getCookie().isSsl());
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int)authenticationProperties.getCookie().getTtl().toSeconds());
        cookie.setPath(SESSION_COOKIE_PATH);
        response.addCookie(cookie);
    }

}
