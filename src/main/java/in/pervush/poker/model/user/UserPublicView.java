package in.pervush.poker.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.UUID;
@Getter
@RequiredArgsConstructor
@ToString
public class UserPublicView {
    @Schema(required = true)
    private final UUID userUuid;
    @Schema(required = true)
    private final String name;

    public static UserPublicView of(final DBUser dbUser) {
        return new UserPublicView(dbUser.userUuid(), dbUser.name());
    }
}
