package in.pervush.poker.model.login;

import jakarta.validation.constraints.NotNull;

public record LoginStep2Request(@NotNull String confirmationCode) {
}
