package in.pervush.poker.service;

import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.FibonacciValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.postgres.VotesMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FibonacciVotesService extends VotesService<FibonacciValue> {

    private static final Scale SCALE = Scale.FIBONACCI;

    public FibonacciVotesService(final VotesMapper mapper, final UsersRepository usersRepository,
                                 final TasksRepository tasksRepository) {
        super(mapper, usersRepository, tasksRepository);
    }

    @Override
    public Map<FibonacciValue, List<String>> getVotesStat(final UUID taskUuid) {
        return super.getVotesStat(taskUuid, SCALE).entrySet().stream()
                .map(v -> Map.entry(FibonacciValue.values()[v.getKey()], v.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean isValidVote(final String vote) {
        try {
            FibonacciValue.valueOf(vote);
            return true;
        } catch (RuntimeException ignored) {}
        return false;
    }

    @Override
    public void createVote(final UUID taskUuid, final UUID userUuid, final String vote) {
        super.createVote(taskUuid, userUuid, FibonacciValue.valueOf(vote));
    }
}
