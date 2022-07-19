package in.pervush.poker.controller;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.ErrorResponse;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.votes.CreateVoteRequest;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.model.votes.VotesStatView;
import in.pervush.poker.service.VotesService;
import in.pervush.poker.utils.RequestHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/tasks/{taskUuid}/votes")
@Tag(name="Votes")
@Validated
@SecurityRequirement(name = "Authorization")
@RequiredArgsConstructor
public class VotesController {

    private final List<VotesService> services;
    private final RequestHelper requestHelper;

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
    public void createVote(@PathVariable("taskUuid") final UUID taskUuid,
                           @RequestBody @Valid CreateVoteRequest request) {
        final var vote = request.getValue();
        getService(vote).createVote(taskUuid, requestHelper.getAuthenticatedUserUuid(), vote);
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
    public List<VotesStatView> getVotes(@PathVariable("taskUuid") final UUID taskUuid) {
        requestHelper.getAuthenticatedUserUuid();
        for (final var service : services) {
            try {
                return service.getVotesStat(taskUuid).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(v -> new VotesStatView(v.getKey(), v.getValue()))
                        .collect(Collectors.toList());
            } catch (NotFoundException ignored) {}
        }
        throw new NotFoundException();
    }

    private VotesService getService(VoteValue vote) {
        for (final var service : services) {
            if (service.isValidVote(vote)) {
                return service;
            }
        }
        throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
    }

}
