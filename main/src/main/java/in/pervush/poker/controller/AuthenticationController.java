package in.pervush.poker.controller;

import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.utils.auth.RequestHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public abstract class AuthenticationController {

    private final UsersRepository usersRepository;
    private final RequestHelper requestHelper;

    protected ResponseEntity<Void> login(final String email, final String password) {
        try {
            final var user = usersRepository.getUser(email, password);

            requestHelper.setAuthCookie(user.userUuid());

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (UserNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
