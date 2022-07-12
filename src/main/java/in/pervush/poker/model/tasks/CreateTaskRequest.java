package in.pervush.poker.model.tasks;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateTaskRequest {
    @NotNull
    private Scale scale;
    @NotBlank
    private String name;
    private String url;
}
