package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
import in.pervush.poker.exception.NotFoundException;
import in.pervush.poker.model.ErrorStatus;
import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.postgres.VotesMapper;
import in.pervush.poker.utils.InstantUtils;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class VotesService {

    private final VotesMapper mapper;
    private final UsersRepository usersRepository;
    private final TasksRepository tasksRepository;
    private final Scale scale;

    public boolean isValidVote(final VoteValue vote) {
        return vote.getScale() == scale;
    }

    public void createVote(final UUID taskUuid, final UUID userUuid, final VoteValue voteValue) {
        if (!isValidVote(voteValue)) {
            throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
        }
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, userUuid);
        validateTaskStatusActive(dbTask);
        validateTaskScale(dbTask, voteValue);
        usersRepository.getUser(userUuid);
        mapper.createVote(taskUuid, userUuid, voteValue, InstantUtils.now());
    }

    public Map<VoteValue, List<String>> getVotesStat(final UUID taskUuid, final UUID userUuid) {
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, userUuid);
        if (dbTask.scale() != scale) {
            throw new NotFoundException();
        }
        if (!dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        return mapper.getVotes(taskUuid).stream()
                .collect(Collectors.groupingBy(DBVote::vote, Collectors.mapping(DBVote::userName, Collectors.toList())));
    }

    private static void validateTaskStatusActive(final DBTask dbTask) {
        if (dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
    }

    private void validateTaskScale(final DBTask dbTask, final VoteValue voteValue) {
        if (dbTask.scale() != voteValue.getScale()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
        }
    }
}
