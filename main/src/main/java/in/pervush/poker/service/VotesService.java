package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.TeamNotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.votes.DBUserVoteStat;
import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.VotesRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VotesService {

    private final VotesRepository votesRepository;
    private final UsersRepository usersRepository;
    private final TasksRepository tasksRepository;
    private final TeamsService teamsService;

    public VotesService(VotesRepository votesRepository, UsersRepository usersRepository,
                        TasksRepository tasksRepository, TeamsService teamsService) {
        this.votesRepository = votesRepository;
        this.usersRepository = usersRepository;
        this.tasksRepository = tasksRepository;
        this.teamsService = teamsService;
    }


    public void createVote(final UUID taskUuid, final UUID teamUuid, final UUID userUuid, final VoteValue voteValue) {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, userUuid);
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, teamUuid, userUuid);

        if (dbTask.scale() != voteValue.scale) {
            throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
        }

        validateTaskStatusActive(dbTask);
        validateTaskScale(dbTask, voteValue);
        usersRepository.getUser(userUuid);
        votesRepository.createVote(taskUuid, userUuid, voteValue);
    }

    public List<DBVote> getVotes(final UUID taskUuid, final UUID teamUuid, final UUID requestingUserUuid) {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, requestingUserUuid);
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, teamUuid, requestingUserUuid);
        if (!dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        return votesRepository.getVotes(taskUuid);
    }

    public List<UUID> getVotedUserUuids(final UUID taskUuid, final UUID requestingUserUuid, final UUID teamUuid) {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, requestingUserUuid);
        tasksRepository.getNotDeletedTask(taskUuid, teamUuid, requestingUserUuid);
        return votesRepository.getVotes(taskUuid).stream().map(DBVote::userUuid).collect(Collectors.toList());
    }

    public List<DBUserVoteStat> getVotesStat(final UUID teamUuid, final UUID requestingUserUuid, final Instant startDtm,
                                             final Instant endDtm) throws TeamNotFoundException {
        teamsService.validateTeamMemberAndGetTeam(teamUuid, requestingUserUuid);
        return votesRepository.getVotesStat(teamUuid, startDtm, endDtm);
    }

    private static void validateTaskStatusActive(final DBTask dbTask) {
        if (dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
    }

    private void validateTaskScale(final DBTask dbTask, final VoteValue voteValue) {
        if (dbTask.scale() != voteValue.scale) {
            throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
        }
    }
}
