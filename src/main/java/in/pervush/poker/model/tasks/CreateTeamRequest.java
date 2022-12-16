package in.pervush.poker.model.tasks;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CreateTeamRequest {
    @NotBlank
    private String teamName;
}
