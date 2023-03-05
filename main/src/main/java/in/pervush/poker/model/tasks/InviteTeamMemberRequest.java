package in.pervush.poker.model.tasks;

import jakarta.validation.constraints.Email;

public record InviteTeamMemberRequest(
        @Email
        String email
) {
}
