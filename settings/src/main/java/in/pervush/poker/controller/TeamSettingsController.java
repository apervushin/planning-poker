package in.pervush.poker.controller;

import in.pervush.poker.exception.SettingsNotFoundException;
import in.pervush.poker.model.TeamSettingsGlobalView;
import in.pervush.poker.model.TeamSettingsUserView;
import in.pervush.poker.model.TeamSettingsView;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.service.UserTeamSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/teams/{teamUuid}/settings")
@Tag(name="Team settings")
@Validated
@SecurityRequirement(name = "Authorization")
public class TeamSettingsController {

    private final UserTeamSettingsService userTeamSettingsService;

    public TeamSettingsController(UserTeamSettingsService userTeamSettingsService) {
        this.userTeamSettingsService = userTeamSettingsService;
    }

    @Operation(
            summary = "Get team settings",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TeamSettingsView> getSettings(@PathVariable("teamUuid") final UUID teamUuid,
                                                        @AuthenticationPrincipal final UserDetailsImpl user) {
        try {
            final var userTeamSettings = userTeamSettingsService
                    .getUserTeamSettings(teamUuid, user.userUuid());
            return ResponseEntity.ok(new TeamSettingsView(
                    TeamSettingsUserView.of(userTeamSettings),
                    new TeamSettingsGlobalView()
            ));
        } catch (final SettingsNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(
            summary = "Set user team settings",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content())
            }
    )
    @PutMapping(value = "/user", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> setUserSettings(@PathVariable("teamUuid") final UUID teamUuid,
                                @AuthenticationPrincipal final UserDetailsImpl user,
                                @RequestBody @Valid TeamSettingsUserView request) {
        try {
            userTeamSettingsService.setUserTeamSettings(teamUuid, user.userUuid(),
                    request.newTasksPushNotificationsEnabled());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (final SettingsNotFoundException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Operation(
            summary = "Set global team settings",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content())
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/global", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void setGlobalSettings(@PathVariable("teamUuid") final UUID teamUuid,
                                  @AuthenticationPrincipal final UserDetailsImpl user,
                                  @RequestBody @Valid TeamSettingsGlobalView request) {

    }
}
