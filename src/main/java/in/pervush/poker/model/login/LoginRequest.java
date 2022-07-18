package in.pervush.poker.model.login;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record LoginRequest(@NotNull @Email String email, @NotBlank String password) {
}
