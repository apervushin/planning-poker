package in.pervush.poker.utils.auth;

import in.pervush.poker.exception.InvalidJwtTokenException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.repository.UsersRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class JwtTokenFilter extends OncePerRequestFilter {

    public static final String AUTH_HEADER_NAME = "Authorization";
    private final UsersRepository usersRepository;
    private final RequestHelper requestHelper;

    public JwtTokenFilter(UsersRepository usersRepository, RequestHelper requestHelper) {
        this.usersRepository = usersRepository;
        this.requestHelper = requestHelper;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final var cookie = WebUtils.getCookie(request, RequestHelper.SESSION_COOKIE_NAME);
        final var cookieToken = cookie == null ? null : cookie.getValue();
        final var headerToken = request.getHeader(AUTH_HEADER_NAME);

        final UUID userUuid;
        try {
            userUuid = requestHelper.getUserUuid(ObjectUtils.firstNonNull(headerToken, cookieToken));
        } catch (final InvalidJwtTokenException e) {
            setUnauthorized(request, response, filterChain);
            return;
        }

        final DBUser user;
        try {
            user = usersRepository.getUser(userUuid);
        } catch (final UserNotFoundException ex) {
            setUnauthorized(request, response, filterChain);
            return;
        }

        final var userDetails = new UserDetailsImpl(user.email(), user.userUuid());

        final var authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private static void setUnauthorized(final HttpServletRequest request, final HttpServletResponse response,
                                        final FilterChain filterChain) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        filterChain.doFilter(request, response);
    }
}
