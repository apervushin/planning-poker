package in.pervush.poker.model.tasks;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(
        @NotBlank
        String teamName
) {
}
