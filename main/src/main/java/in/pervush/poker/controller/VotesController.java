package in.pervush.poker.controller;

import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.user.UserDetailsImpl;
import in.pervush.poker.model.user.UserPublicView;
import in.pervush.poker.model.votes.CreateVoteRequest;
import in.pervush.poker.model.votes.VotesStatView;
import in.pervush.poker.service.UserService;
import in.pervush.poker.service.VotesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/teams/{teamUuid}/tasks/{taskUuid}/votes")
@Tag(name="Task votes")
@Validated
@SecurityRequirement(name = "Authorization")
public class VotesController {

    private final VotesService votesService;
    private final UserService userService;

    public VotesController(VotesService votesService, UserService userService) {
        this.votesService = votesService;
        this.userService = userService;
    }

    @Operation(
            summary = "Vote",
            responses = {
                    @ApiResponse(responseCode = "201"),
                    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", content = @Content())
            }
    )
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createVote(@PathVariable("teamUuid") final UUID teamUuid,
                           @PathVariable("taskUuid") final UUID taskUuid,
                           @RequestBody @Valid CreateVoteRequest request,
                           @AuthenticationPrincipal final UserDetailsImpl user) {
        final var vote = request.value();
        votesService.createVote(taskUuid, teamUuid, user.userUuid(), vote);
    }

    @Operation(
            summary = "Get votes stat",
            responses = {
                    @ApiResponse(responseCode = "200"),
                    @ApiResponse(responseCode = "401", content = @Content()),
                    @ApiResponse(responseCode = "404", content = @Content())
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VotesStatView> getVotes(@PathVariable("teamUuid") final UUID teamUuid,
                                        @PathVariable("taskUuid") final UUID taskUuid,
                                        @AuthenticationPrincipal final UserDetailsImpl user) {
        return votesService.getVotes(taskUuid, teamUuid, user.userUuid()).stream()
                .map(v -> Pair.of(v, userService.getUser(v.userUuid())))
                .collect(Collectors.groupingBy(
                        a -> a.getLeft().vote(),
                        Collectors.mapping(Pair::getRight, Collectors.toList()))
                )
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(v -> new VotesStatView(
                        v.getKey(),
                        v.getValue().stream().map(UserPublicView::of).collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
