package in.pervush.poker.controller;

import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.tasks.CreateTeamRequest;
import in.pervush.poker.model.teams.MembershipStatus;
import in.pervush.poker.model.teams.UserTeamView;
import in.pervush.poker.service.TeamsService;
import in.pervush.poker.service.UserService;
import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/teams")
@Tag(name="Teams")
@RequiredArgsConstructor
@Validated
public class TeamsController {

    private final TeamsService teamsService;
    private final UserService userService;
    private final RequestHelper requestHelper;

    @Operation(
            summary = "Create team",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserTeamView createTeam(@RequestBody final CreateTeamRequest request) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        final var userTeam = teamsService.createTeam(userUuid, request.getTeamName());
        return UserTeamView.of(userTeam, userService.getUser(userUuid));
    }

    @Operation(
            summary = "Get teams",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserTeamView> getUserTeams(
            @RequestParam(name = "membershipStatus", required = false) MembershipStatus membershipStatus
    ) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        return teamsService.getTeams(userUuid, membershipStatus).stream()
                .map(v -> UserTeamView.of(v, userService.getUser(v.userUuid()))).toList();
    }

    @Operation(
            summary = "Delete team",
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content(), description = "Team not exists or you are not owner of the team")
            }
    )
    @DeleteMapping(value = "/{teamUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable final UUID teamUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        teamsService.deleteTeam(teamUuid, userUuid);
    }

    @Operation(
            summary = "Leave team",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "403", content = @Content(), description = "Team not exists or you are not owner of the team")
            }
    )
    @DeleteMapping(value = "/{teamUuid}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveTeam(@PathVariable final UUID teamUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        teamsService.deleteTeamMember(teamUuid, userUuid, userUuid);
    }
}
