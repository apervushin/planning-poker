package in.pervush.poker.model.user;

import java.time.Instant;
import java.util.UUID;

public record DBUser(UUID userUuid, String email, String passwordEncoded, String name, Instant createDtm,
                     UUID emailConfirmationCode) {
}
