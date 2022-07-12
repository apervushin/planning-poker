package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.tasks.DBTask;
import in.pervush.poker.model.tasks.Scale;
import in.pervush.poker.model.tasks.Status;
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
            @Arg(column = "status", javaType = Status.class),
            @Arg(column = "create_dtm", javaType = Instant.class),
    })
    @Select("select * " +
            "from tasks " +
            "where user_uuid = #{userUuid} and status <> cast(#{excludedStatus} as task_status) " +
            "order by create_dtm desc")
    List<DBTask> getTasks(@Param("userUuid") UUID userUuid, @Param("excludedStatus") Status excludedStatus);

    @ResultMap("task")
    @Select("select * " +
            "from tasks " +
            "where task_uuid = #{taskUuid} and status <> cast(#{excludedStatus} as task_status)")
    Optional<DBTask> getTask(@Param("taskUuid") UUID taskUuid, @Param("excludedStatus") Status excludedStatus);

    @ResultMap("task")
    @Select("select * " +
            "from tasks " +
            "where task_uuid = #{taskUuid} and user_uuid = #{userUuid} and status <> cast(#{excludedStatus} as task_status) " +
            "for update")
    Optional<DBTask> getTaskLock(@Param("taskUuid") UUID taskUuid,
                                 @Param("userUuid") UUID userUuid,
                                 @Param("excludedStatus") Status excludedStatus);

    @Update("update tasks set status = cast(#{status} as task_status) where task_uuid = #{taskUuid}")
    void setTaskStatusByTaskUuid(@Param("taskUuid") UUID taskUuid, @Param("status") Status status);

    @Update("update tasks " +
            "set status = cast(#{status} as task_status) " +
            "where task_uuid = #{taskUuid} and user_uuid = #{userUuid}")
    boolean setTaskStatusByTaskUuidAnUserUuid(@Param("taskUuid") UUID taskUuid,
                                              @Param("userUuid") UUID userUuid,
                                              @Param("status") Status status);

    @Insert("insert into tasks(user_uuid, task_uuid, name, url, scale, status, create_dtm) " +
            "values(#{userUuid}, #{taskUuid}, #{name}, #{url}, cast(#{scale} as task_scale), cast(#{status} as task_status), #{createDtm})")
    void createTask(@Param("userUuid") UUID userUuid,
                    @Param("taskUuid") UUID taskUuid,
                    @Param("name") String name,
                    @Param("url") String url,
                    @Param("scale") Scale scale,
                    @Param("status") Status status,
                    @Param("createDtm") Instant createDtm);
}
