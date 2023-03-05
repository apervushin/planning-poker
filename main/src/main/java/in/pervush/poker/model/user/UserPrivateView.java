package in.pervush.poker.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public class UserPrivateView extends UserPublicView {

    @Schema(required = true)
    public final String email;

    private UserPrivateView(final UUID userUuid, final String name, final String email) {
        super(userUuid, name);
        this.email = email;
    }

    public static UserPrivateView of(final DBUser dbUser) {
        return new UserPrivateView(dbUser.userUuid(), dbUser.name(), dbUser.email());
    }
}
