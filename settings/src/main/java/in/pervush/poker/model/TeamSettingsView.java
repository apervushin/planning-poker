package in.pervush.poker.model;

import jakarta.validation.constraints.NotNull;

public record TeamSettingsView(
        @NotNull TeamSettingsUserView user,
        @NotNull TeamSettingsGlobalView global
) {

}
