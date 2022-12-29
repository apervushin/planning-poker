package in.pervush.poker.controller;

import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.utils.auth.RequestHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RequiredArgsConstructor
public abstract class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final RequestHelper requestHelper;

    protected ResponseEntity<Void> login(final String email, final String password) {
        try {
            final var authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(email, password));

            final var user = (UserDetailsImpl) authenticate.getPrincipal();

            requestHelper.setAuthCookie(user);

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
