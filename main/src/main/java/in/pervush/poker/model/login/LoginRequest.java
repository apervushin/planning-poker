package in.pervush.poker.model.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Deprecated
public record LoginRequest(@NotNull @Email String email, @NotBlank String password) {
}
