package in.pervush.poker.utils;

import in.pervush.poker.exception.UnauthorizedException;
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

    public static final String USER_UUID_COOKIE_NAME = "x-user-uuid";
    private static final String COOKIE_PATH = "/api/";

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public UUID getUserUuidCookie() {
        final var cookie = WebUtils.getCookie(request, USER_UUID_COOKIE_NAME);
        if (cookie == null) {
            throw new UnauthorizedException();
        }

        try {
            return UUID.fromString(cookie.getValue());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new UnauthorizedException();
        }
    }

    public void setUserUuidCookie(final UUID userUuid) {
        final var cookie = new Cookie(USER_UUID_COOKIE_NAME, userUuid.toString());
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(Integer.MAX_VALUE);
        cookie.setPath(COOKIE_PATH);
        response.addCookie(cookie);
    }

}
