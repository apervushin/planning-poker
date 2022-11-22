package in.pervush.poker.model.notifications.push;

import javax.validation.constraints.NotEmpty;

public record SetPushTokenRequest(@NotEmpty String token) {
}
