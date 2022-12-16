package in.pervush.poker.model.notifications.push;

import jakarta.validation.constraints.NotEmpty;

public record SetPushTokenRequest(@NotEmpty String token) {
}
