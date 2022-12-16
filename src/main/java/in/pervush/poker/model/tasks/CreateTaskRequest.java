package in.pervush.poker.model.tasks;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateTaskRequest {
    @NotNull
    private Scale scale;
    @NotBlank
    private String name;
    private String url;
}
