package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface VotesMapper {

    @Insert("""
            insert into votes(task_uuid, user_uuid, vote, create_dtm)
            values(#{taskUuid}, #{votingUserUuid}, cast(#{voteValue} as task_vote), #{createDtm})
            on conflict(task_uuid, user_uuid) do update set vote = excluded.vote, create_dtm = excluded.create_dtm
            """)
    void createVote(@Param("taskUuid") UUID taskUuid,
                    @Param("votingUserUuid") UUID votingUserUuid,
                    @Param("voteValue") VoteValue vote,
                    @Param("createDtm") Instant createDtm);

    @ConstructorArgs(value = {
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "vote", javaType = VoteValue.class)
    })
    @Select("""
            select user_uuid, vote
            from votes
            where task_uuid = #{taskUuid}
            order by vote, user_uuid
            """)
    List<DBVote> getVotes(@Param("taskUuid") UUID taskUuid);
}
