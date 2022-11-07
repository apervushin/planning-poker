package in.pervush.poker.controller;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.UserAlreadyAddedException;
import in.pervush.poker.exception.UserNotFoundException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.InviteTeamMemberRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/teams/{teamUuid}/members")
@Tag(name="Team members")
@RequiredArgsConstructor
@Validated
public class TeamMembersController {

    private final TeamsService teamsService;
    private final UserService userService;
    private final RequestHelper requestHelper;

    @Operation(
            summary = "Get team members",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserTeamView> getTeamMembers(@PathVariable("teamUuid") final UUID teamUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        return teamsService.getTeamMembers(teamUuid, userUuid).stream()
                .map(v -> UserTeamView.of(v, userService.getUser(v.userUuid())))
                .toList();
    }

    @Operation(
            summary = "Invite team member",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content(), description = "You are not team owner or team does not exist"),
            }
    )
    @PostMapping(
            path = "/invite",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteTeamMember(@PathVariable("teamUuid") final UUID teamUuid,
                                 @RequestBody @Valid final InviteTeamMemberRequest request) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        try {
            teamsService.inviteTeamMember(teamUuid, userUuid, request.getEmail());
        } catch (UserAlreadyAddedException ex) {
            throw new ErrorStatusException(ErrorStatus.USER_ALREADY_ADDED);
        } catch (UserNotFoundException ex) {
            throw new ErrorStatusException(ErrorStatus.USER_EMAIL_NOT_EXISTS);
        }
    }

    @Operation(
            summary = "Accept team invitation",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content(), description = "User has no team invitation or team does not exist or user is already team member")
            }
    )
    @GetMapping(path = "/accept")
    public void acceptTeamInvitation(@PathVariable("teamUuid") final UUID teamUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        teamsService.acceptTeamInvitation(teamUuid, userUuid);
    }

    @Operation(
            summary = "Deleted team member",
            responses = {
                    @ApiResponse(responseCode = "204"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @DeleteMapping(value = "/{userUuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeamMember(@PathVariable("teamUuid") final UUID teamUuid,
                                 @PathVariable("userUuid") final UUID deletingUserUuid) {
        final var userUuid = requestHelper.getAuthenticatedUserUuid();
        teamsService.deleteTeamMember(teamUuid, userUuid, deletingUserUuid);
    }
}
