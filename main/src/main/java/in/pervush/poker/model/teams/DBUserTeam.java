package in.pervush.poker.model.teams;

import java.time.Instant;
import java.util.UUID;

public record DBUserTeam(UUID teamUuid, String teamName, UUID userUuid, Instant teamCreateDtm,
                         MembershipStatus membershipStatus) {
}
