package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.DBUserTeamSettings;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;
import java.util.UUID;

public interface UserTeamSettingsMapper {

    @Insert("""
            insert into user_team_settings(team_uuid, user_uuid, notifications_enabled)
            values(#{settings.teamUuid}, #{settings.userUuid}, #{settings.notificationsEnabled})
            """)
    void createUser(@Param("settings") DBUserTeamSettings settings);

    @Delete("delete from user_team_settings where team_uuid = #{teamUuid}")
    boolean deleteTeam(@Param("teamUuid") UUID teamUuid);

    @Delete("delete from user_team_settings where team_uuid = #{teamUuid} and user_uuid = #{userUuid}")
    boolean deleteUser(@Param("teamUuid") UUID teamUuid, @Param("userUuid") UUID userUuid);

    @Results(id = "userTeamSettings")
    @ConstructorArgs({
            @Arg(column = "team_uuid", javaType = UUID.class),
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "notifications_enabled", javaType = boolean.class)
    })
    @Select("select * from user_team_settings where team_uuid = #{teamUuid} and user_uuid = #{userUuid}")
    Optional<DBUserTeamSettings> getUserTeamSettings(@Param("teamUuid") UUID teamUuid,
                                                     @Param("userUuid") UUID userUuid);

    @ResultMap("userTeamSettings")
    @Update("""
            update user_team_settings
            set notifications_enabled = #{newTasksPushNotificationsEnabled}
            where team_uuid = #{teamUuid} and user_uuid = #{userUuid}
            """)
    boolean setUserTeamSettings(@Param("teamUuid") UUID teamUuid,
                                @Param("userUuid") UUID userUuid,
                                @Param("newTasksPushNotificationsEnabled") boolean newTasksPushNotificationsEnabled);
}
