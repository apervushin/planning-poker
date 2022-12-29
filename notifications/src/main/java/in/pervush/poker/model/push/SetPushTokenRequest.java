package in.pervush.poker.model.push;

import jakarta.validation.constraints.NotEmpty;

public record SetPushTokenRequest(@NotEmpty String token) {
}
