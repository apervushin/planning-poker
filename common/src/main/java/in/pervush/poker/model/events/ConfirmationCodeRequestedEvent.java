package in.pervush.poker.model.events;

public record ConfirmationCodeRequestedEvent(String email, String confirmationCode) {
}
