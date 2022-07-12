package in.pervush.poker.model.votes;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVoteRequest {

    @Schema(required = true)
    @NotBlank
    private VoteValue value;
}
