package in.pervush.poker.model.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LoginStep2ResponseView(
        @NotNull LoginStep1Status status,
        @Schema(description = "Returned only for EXISTING_USER flow") String accessToken) {

    public enum LoginStep1Status {
        EXISTING_USER,
        NEW_USER
    }
}
