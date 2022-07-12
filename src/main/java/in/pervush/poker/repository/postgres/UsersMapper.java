package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.user.DBUser;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UsersMapper {

    @Insert("insert into users(user_uuid, name, create_dtm) values(#{userUuid}, #{name}, #{createDtm})")
    void createUser(@Param("userUuid") UUID userUuid,
                    @Param("name") String name,
                    @Param("createDtm") Instant createDtm);

    @Results(id = "user")
    @ConstructorArgs(value = {
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "create_dtm", javaType = Instant.class),
    })
    @Select("select * from users where user_uuid = #{userUuid}")
    Optional<DBUser> getUser(@Param("userUuid") UUID userUuid);
}
