package in.pervush.poker.model.registration;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record RegisterRequest(@NotNull @Email String email, @NotEmpty String password, @NotBlank String name) {

}
