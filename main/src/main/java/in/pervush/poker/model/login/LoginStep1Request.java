package in.pervush.poker.model.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record LoginStep1Request(@NotNull @Email String email) {
}
