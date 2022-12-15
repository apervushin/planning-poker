package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.votes.DBUserVoteStat;
import in.pervush.poker.model.votes.DBVote;
import in.pervush.poker.model.votes.VoteValue;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
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

    @ConstructorArgs(value = {
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "votes_cnt", javaType = int.class)
    })
    @Select("""
            with v as (
                select
                    v.user_uuid,
                    count(v.task_uuid) as votes_cnt
                from tasks t
                inner join votes v on t.task_uuid = v.task_uuid
                where t.team_uuid = #{teamUuid}
                    and t.create_dtm between #{startDtm} and #{endDtm}
                    and t.is_finished
                    and not t.is_deleted
                group by v.user_uuid
            )
            select
                ut.user_uuid,
                coalesce(v.votes_cnt, 0) as votes_cnt
            from users_x_teams ut
            left join v on ut.user_uuid = v.user_uuid
            where ut.team_uuid = #{teamUuid}
            order by votes_cnt desc
            """)
    List<DBUserVoteStat> getVotesStat(@Param("teamUuid") UUID teamUuid,
                                      @Param("startDtm") Instant startDtm,
                                      @Param("endDtm") Instant endDtm);

    @Delete("delete from votes where task_uuid = #{taskUuid}")
    void eraseVotes(@Param("taskUuid") UUID taskUuid);

    @Select("""
            select
                count(t.task_uuid) - count(v.vote) as count
            from tasks t
            left join votes v on t.task_uuid = v.task_uuid and v.user_uuid = #{userUuid}
            where not t.is_deleted
                and not t.is_finished
                and t.team_uuid = #{teamUuid}
            """)
    int countNotVotedUserTasks(@Param("teamUuid") UUID teamUuid,
                               @Param("userUuid") UUID userUuid);
}
