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
            @Arg(column = "user_name", javaType = String.class),
            @Arg(column = "vote", javaType = VoteValue.class)
    })
    @Select("""
            select u.name as user_name, v.vote
            from votes v
            inner join users u on u.user_uuid = v.user_uuid
            where v.task_uuid = #{taskUuid}
            order by vote, user_name
            """)
    List<DBVote> getVotes(@Param("taskUuid") UUID taskUuid);
}
