package in.pervush.poker.model.tasks;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record DeleteTasksRequest(
        @NotEmpty
        Set<@NotNull UUID> taskUuids
) {
}
