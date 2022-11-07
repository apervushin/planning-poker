package in.pervush.poker.model.tasks;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class InviteTeamMemberRequest {
    @Email
    private String email;
}
