package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.DBUserNotVotedTasksCount;
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

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
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
            @Arg(column = "team_uuid", javaType = UUID.class),
    })
    @Select("""
            <script>
                select
                    t.task_uuid,
                    max(t.user_uuid::varchar)::uuid as user_uuid,
                    max(t.name) as name,
                    max(t.url) as url,
                    max(t.scale) as scale,
                    max(t.create_dtm) as create_dtm,
                    max(t.is_deleted::int)::bool as is_deleted,
                    max(t.is_finished::int)::bool as is_finished,
                    max(case when #{requestingUserUuid} = v.user_uuid then v.vote else null end) as vote,
                    max(t.team_uuid::varchar)::uuid as team_uuid
                from tasks t
                left join votes v on t.task_uuid = v.task_uuid
                where not t.is_deleted
                    and t.team_uuid = #{teamUuid}
                    <if test="search != null">
                        and (
                            lower(t.name) like '%'||lower(#{search})||'%'
                            or lower(t.url) like '%'||lower(#{search})||'%'
                        )
                    </if>
                    <if test="finished != null">
                        and t.is_finished = #{finished}
                    </if>
                group by t.task_uuid
                order by create_dtm desc
            </script>
            """)
    List<DBTask> getNotDeletedTasks(@Param("teamUuid") UUID teamUuid,
                                    @Param("requestingUserUuid") UUID requestingUserUuid,
                                    @Nullable @Param("search") String search,
                                    @Nullable @Param("finished") Boolean finished);

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
            where not t.is_deleted and t.task_uuid = #{taskUuid} and t.team_uuid = #{teamUuid}
            """)
    Optional<DBTask> getNotDeletedTask(@Param("taskUuid") UUID taskUuid,
                                       @Param("teamUuid") UUID teamUuid,
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
                    max(case when #{user} = user_uuid then vote else null end) as vote,
                    count(*) as votes_cnt
                from votes
                where task_uuid = #{taskUuid}
                group by task_uuid
            ) v on t.task_uuid = v.task_uuid
            where not t.is_deleted
                and t.task_uuid = #{taskUuid}
                and t.team_uuid = #{teamUuid}
            for update of t
            """)
    Optional<DBTask> getNotDeletedTaskLock(@Param("taskUuid") UUID taskUuid,
                                           @Param("teamUuid") UUID teamUuid,
                                           @Param("user") UUID userUuid);

    @Update("""
            update tasks
            set is_finished = #{finished}
            where not is_deleted and team_uuid = #{teamUuid} and task_uuid = #{taskUuid}
            """)
    boolean setFinished(@Param("taskUuid") UUID taskUuid,
                        @Param("teamUuid") UUID teamUuid,
                        @Param("finished") boolean finished);

    @Update("""
            <script>
                update tasks
                set is_deleted = true
                where not is_deleted and team_uuid = #{teamUuid}
                    and <foreach item="taskUuid" index="index" collection="taskUuids"
                                 open="task_uuid in (" separator="," close=")" nullable="false">
                          #{taskUuid}
                        </foreach>
            </script>
            """)
    boolean setDeleted(@Param("taskUuids") Collection<UUID> taskUuids,
                       @Param("teamUuid") UUID teamUuid);

    @Insert("""
            insert into tasks(user_uuid, task_uuid, name, url, scale, create_dtm, team_uuid)
            values(#{user}, #{taskUuid}, #{name}, #{url}, cast(#{scale} as task_scale), #{createDtm}, #{teamUuid})
            """)
    void createTask(@Param("user") UUID userUuid,
                    @Param("taskUuid") UUID taskUuid,
                    @Param("name") String name,
                    @Param("url") String url,
                    @Param("scale") Scale scale,
                    @Param("createDtm") Instant createDtm,
                    @Param("teamUuid") UUID teamUuid);

    @Select("""
            select count(*) as cnt
            from tasks
            where not is_deleted
                and is_finished
                and team_uuid = #{teamUuid}
                and create_dtm between #{startDtm} and #{endDtm}
            """)
    int getFinishedTasksCount(@Param("teamUuid") UUID teamUuid,
                              @Param("startDtm") Instant startDtm,
                              @Param("endDtm") Instant endDtm);


    @ConstructorArgs({
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "not_voted_tasks_cnt", javaType = int.class)
    })
    @Select("""
            with total_tasks_count as (
                select count(*) as cnt
                from tasks
                where team_uuid = #{teamUuid}
                    and not is_deleted
                    and not is_finished
            ),
            users_votes_count as (
                select
                    user_uuid,
                    count(*) as cnt
                from votes
                where task_uuid in (
                    select task_uuid
                    from tasks
                    where team_uuid = #{teamUuid}
                )
                group by user_uuid
            )
            select
                ut.user_uuid,
                tc.cnt - coalesce(uv.cnt, 0) as not_voted_tasks_cnt
            from users_x_teams ut
            left join users_votes_count uv on ut.user_uuid = uv.user_uuid
            left join total_tasks_count tc on 1=1
            where team_uuid = #{teamUuid}
                and membership_status in ('MEMBER', 'OWNER')
            """)
    List<DBUserNotVotedTasksCount> getUsersNotVotedTasksCount(@Param("teamUuid") UUID teamUuid);
}
