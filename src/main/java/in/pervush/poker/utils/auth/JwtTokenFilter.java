package in.pervush.poker.utils.auth;

import in.pervush.poker.exception.InvalidJwtTokenException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final UsersRepository usersRepository;
    private final RequestHelper requestHelper;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final var cookie = WebUtils.getCookie(request, RequestHelper.SESSION_COOKIE_NAME);
        if (cookie == null) {
            setUnauthorized(request, response, filterChain);
            return;
        }

        final UUID userUuid;
        try {
            userUuid = requestHelper.getUserUuid(cookie.getValue());
        } catch (InvalidJwtTokenException e) {
            setUnauthorized(request, response, filterChain);
            return;
        }

        final DBUser user;
        try {
            user = usersRepository.getUser(userUuid);
        } catch (UserNotFoundException ex) {
            setUnauthorized(request, response, filterChain);
            return;
        }

        final var userDetails = UserDetailsImpl.of(user);

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
