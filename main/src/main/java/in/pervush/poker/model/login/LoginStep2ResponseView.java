package in.pervush.poker.model.login;

import jakarta.validation.constraints.NotNull;

public record LoginStep2ResponseView(@NotNull LoginStep1Status status) {

    public enum LoginStep1Status {
        EXISTING_USER,
        NEW_USER
    }
}
