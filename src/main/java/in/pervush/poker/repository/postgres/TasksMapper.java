package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.votes.VoteValue;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TasksMapper {

    @Results(id = "task")
    @ConstructorArgs(value = {
            @Arg(column = "task_uuid", javaType = UUID.class),
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "url", javaType = String.class),
            @Arg(column = "scale", javaType = Scale.class),
            @Arg(column = "is_finished", javaType = boolean.class),
            @Arg(column = "create_dtm", javaType = Instant.class),
            @Arg(column = "vote", javaType = VoteValue.class),
    })
    @Select("""
            select
                t.task_uuid,
                max(t.user_uuid::varchar)::uuid as user_uuid,
                max(t.name) as name,
                max(t.url) as url,
                max(t.scale) as scale,
                max(t.create_dtm) as create_dtm,
                max(t.is_deleted::int)::bool as is_deleted,
                max(t.is_finished::int)::bool as is_finished,
                max(case when t.user_uuid = v.user_uuid then v.vote else null end) as vote
            from tasks t
            left join votes v on t.task_uuid = v.task_uuid
            where not t.is_deleted
                and t.user_uuid = #{userUuid}
            group by t.task_uuid
            order by create_dtm desc
            """)
    List<DBTask> getNotDeletedTasks(@Param("userUuid") UUID userUuid);

    @ResultMap("task")
    @Select("""
            select
                t.*,
                v.vote
            from tasks t
            left join (
                select
                    task_uuid,
                    max(case when #{requestingUserUuid} = user_uuid then vote else null end) as vote,
                    count(*) as votes_cnt
                from votes
                where task_uuid = #{taskUuid}
                group by task_uuid
            ) v on t.task_uuid = v.task_uuid
            where not t.is_deleted and t.task_uuid = #{taskUuid}
            """)
    Optional<DBTask> getNotDeletedTask(@Param("taskUuid") UUID taskUuid,
                                       @Param("requestingUserUuid") UUID requestingUserUuid);

    @ResultMap("task")
    @Select("""
            select
                t.*,
                v.vote
            from tasks t
            left join (
                select
                    task_uuid,
                    max(case when #{userUuid} = user_uuid then vote else null end) as vote,
                    count(*) as votes_cnt
                from votes
                where task_uuid = #{taskUuid}
                group by task_uuid
            ) v on t.task_uuid = v.task_uuid
            where not t.is_deleted
                and t.task_uuid = #{taskUuid}
                and t.user_uuid = #{userUuid}
            for update of t
            """)
    Optional<DBTask> getNotDeletedTaskLock(@Param("taskUuid") UUID taskUuid,
                                           @Param("userUuid") UUID userUuid);

    @Update("""
            update tasks
            set is_finished = true
            where not is_deleted and task_uuid = #{taskUuid} and user_uuid = #{userUuid}
            """)
    boolean setFinished(@Param("taskUuid") UUID taskUuid,
                        @Param("userUuid") UUID userUuid);

    @Update("""
            update tasks
            set is_deleted = true
            where not is_deleted and task_uuid = #{taskUuid} and user_uuid = #{userUuid}
            """)
    boolean setDeleted(@Param("taskUuid") UUID taskUuid,
                       @Param("userUuid") UUID userUuid);

    @Insert("""
            insert into tasks(user_uuid, task_uuid, name, url, scale, create_dtm)
            values(#{userUuid}, #{taskUuid}, #{name}, #{url}, cast(#{scale} as task_scale), #{createDtm})
            """)
    void createTask(@Param("userUuid") UUID userUuid,
                    @Param("taskUuid") UUID taskUuid,
                    @Param("name") String name,
                    @Param("url") String url,
                    @Param("scale") Scale scale,
                    @Param("createDtm") Instant createDtm);
}
