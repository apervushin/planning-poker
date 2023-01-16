package in.pervush.poker.model.registration;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Deprecated
public record RegisterRequest(@NotNull @Email String email, @NotEmpty String password, @NotBlank String name) {

}
