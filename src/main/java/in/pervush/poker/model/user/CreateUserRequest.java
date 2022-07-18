package in.pervush.poker.model.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record CreateUserRequest(@NotNull @Email String email, @NotEmpty String password, @NotBlank String name) {

}
