package in.pervush.poker.model.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotNull
        Scale scale,

        @NotBlank
        String name,

        String url
) {
}
