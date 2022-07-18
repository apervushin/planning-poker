package in.pervush.poker.service;

import in.pervush.poker.model.user.DBUser;
import in.pervush.poker.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository repository;

    public DBUser login(final String email, final String password) {
        return repository.getUser(email, password);
    }
}
