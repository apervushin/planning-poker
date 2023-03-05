package in.pervush.poker.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;
public class UserPublicView {
    @Schema(required = true)
    public final UUID userUuid;
    @Schema(required = true)
    public final String name;

    public static UserPublicView of(final DBUser dbUser) {
        return new UserPublicView(dbUser.userUuid(), dbUser.name());
    }

    protected UserPublicView(UUID userUuid, String name) {
        this.userUuid = userUuid;
        this.name = name;
    }
}
