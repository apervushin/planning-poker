package in.pervush.poker.service;

import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.repository.TasksRepository;
import in.pervush.poker.repository.UsersRepository;
import in.pervush.poker.repository.postgres.VotesMapper;
import org.springframework.stereotype.Service;

@Service
public class ClothesSizeVotesService extends VotesService {

    private static final Scale SCALE = Scale.CLOTHES_SIZE;

    public ClothesSizeVotesService(final VotesMapper mapper, final UsersRepository usersRepository,
                                   final TasksRepository tasksRepository) {
        super(mapper, usersRepository, tasksRepository, SCALE);
    }

}
