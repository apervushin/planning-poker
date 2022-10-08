package in.pervush.poker.service;

import in.pervush.poker.exception.ErrorStatusException;
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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VotesService {

    private final VotesMapper mapper;
    private final UsersRepository usersRepository;
    private final TasksRepository tasksRepository;

    public void createVote(final UUID taskUuid, final UUID userUuid, final VoteValue voteValue) {
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, userUuid);

        if (dbTask.scale() != voteValue.getScale()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_VOTE_VALUE);
        }

        validateTaskStatusActive(dbTask);
        validateTaskScale(dbTask, voteValue);
        usersRepository.getUser(userUuid);
        mapper.createVote(taskUuid, userUuid, voteValue, InstantUtils.now());
    }

    public List<DBVote> getVotes(final UUID taskUuid, final UUID requestingUserUuid) {
        final var dbTask = tasksRepository.getNotDeletedTask(taskUuid, requestingUserUuid);
        if (!dbTask.finished()) {
            throw new ErrorStatusException(ErrorStatus.INVALID_TASK_STATUS);
        }
        return mapper.getVotes(taskUuid);
    }

    public List<UUID> getVotedUserUuids(final UUID taskUuid, final UUID requestingUserUuid) {
        tasksRepository.getNotDeletedTask(taskUuid, requestingUserUuid);
        return mapper.getVotes(taskUuid).stream().map(DBVote::userUuid).collect(Collectors.toList());
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
