package in.pervush.poker.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserView(@Schema(required = true) String email, @Schema(required = true) String name) {

    public static UserView of(DBUser dbUser) {
        return new UserView(dbUser.email(), dbUser.name());
    }
}
