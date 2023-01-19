package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.push.DBPushToken;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PushTokensMapper {

    @Insert("""
            insert into push_tokens(user_uuid, device_uuid, token, last_update_dtm)
            values(#{userUuid}, #{deviceUuid}, #{token}, #{updateDtm})
            on conflict(user_uuid, device_uuid) do update
            set token = excluded.token, last_update_dtm = #{updateDtm}
            """)
    void setPushToken(@Param("userUuid") UUID userUuid,
                      @Param("deviceUuid") UUID deviceUuid,
                      @Param("token") String token,
                      @Param("updateDtm") Instant updateDtm);

    @Delete("delete from push_tokens where token = #{token}")
    void deleteByPushToken(@Param("token") String token);

    @ConstructorArgs(value = {
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "device_uuid", javaType = UUID.class),
            @Arg(column = "token", javaType = String.class),
            @Arg(column = "last_update_dtm", javaType = Instant.class)
    })
    @Select("""
            <script>
                select
                    user_uuid,
                    device_uuid,
                    token,
                    last_update_dtm
                from (
                    select
                        user_uuid,
                        device_uuid,
                        token,
                        last_update_dtm,
                        row_number() over(partition by user_uuid order by last_update_dtm desc) as rn
                    from push_tokens
                    where
                    <foreach item="userUuid" index="index" collection="usersUuids" open="user_uuid in (" separator="," close=")" nullable="false">
                        #{userUuid}
                    </foreach>
                ) t
                where rn &lt;= #{limitPerUser}
                order by last_update_dtm desc
            </script>
            """)
    List<DBPushToken> getTokens(@Param("usersUuids") Set<UUID> usersUuids,
                                @Param("limitPerUser") int limitPerUser);
}
