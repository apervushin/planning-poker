package in.pervush.poker.model.login;

import jakarta.validation.constraints.NotNull;

public record LoginStep3Request(@NotNull String name) {
}
