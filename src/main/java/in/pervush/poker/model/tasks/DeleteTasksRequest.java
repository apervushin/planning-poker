package in.pervush.poker.model.tasks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteTasksRequest {
    @NotEmpty
    private Set<@NotNull UUID> taskUuids;
}
