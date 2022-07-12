package in.pervush.poker.service;

import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.ClothesSizeValue;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.postgres.VotesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClothesSizeVotesService extends VotesService<ClothesSizeValue> {

    private static final Scale SCALE = Scale.CLOTHES_SIZE;

    public ClothesSizeVotesService(final VotesMapper mapper, final UsersRepository usersRepository,
                                   final TasksRepository tasksRepository) {
        super(mapper, usersRepository, tasksRepository);
    }

    @Override
    public Map<ClothesSizeValue, List<String>> getVotesStat(final UUID taskUuid) {
        return super.getVotesStat(taskUuid, SCALE).entrySet().stream()
                .map(v -> Map.entry(ClothesSizeValue.values()[v.getKey()], v.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean isValidVote(final String vote) {
        try {
            ClothesSizeValue.valueOf(vote);
            return true;
        } catch (RuntimeException ignored) {}
        return false;
    }

    @Override
    public void createVote(final UUID userUuid, final UUID taskUuid, final String vote) {
        super.createVote(taskUuid, userUuid, ClothesSizeValue.valueOf(vote));
    }
}
