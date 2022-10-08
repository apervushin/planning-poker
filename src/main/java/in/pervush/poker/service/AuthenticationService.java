package in.pervush.poker.service;

import in.pervush.poker.repository.AuthenticationRepository;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository usersRepository;
    private final AuthenticationRepository authenticationRepository;

    public String login(final String email, final String password) {
        final var user = usersRepository.getUser(email, password);
        return authenticationRepository.createToken(user);
    }
}
