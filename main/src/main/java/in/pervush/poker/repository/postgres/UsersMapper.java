package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.user.DBUser;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UsersMapper {

    @Insert("""
            insert into users(user_uuid, email, password_encoded, name, create_dtm, email_confirmation_code)
            values(#{user}, #{email}, #{passwordEncoded}, #{name}, #{createDtm}, #{emailConfirmationCode})
            """)
    void createUser(@Param("user") UUID userUuid,
                    @Param("email") String email,
                    @Param("passwordEncoded") String passwordEncoded,
                    @Param("name") String name,
                    @Param("createDtm") Instant createDtm,
                    @Param("emailConfirmationCode") UUID emailConfirmationCode);

    @Results(id = "user")
    @ConstructorArgs(value = {
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "email", javaType = String.class),
            @Arg(column = "password_encoded", javaType = String.class),
            @Arg(column = "name", javaType = String.class),
            @Arg(column = "create_dtm", javaType = Instant.class),
            @Arg(column = "email_confirmation_code", javaType = UUID.class),
    })
    @Select("select * from users where user_uuid = #{user}")
    Optional<DBUser> getUser(@Param("user") UUID userUuid);

    @ResultMap("user")
    @Select("select * from users where email = #{email}")
    Optional<DBUser> getUserByEmail(@Param("email") String email);

    @Update("""
            update users
            set email_confirmation_code = null, email_confirmed = true
            where email_confirmation_code = #{emailConfirmationCode}
            """)
    boolean confirmEmail(@Param("emailConfirmationCode") final UUID emailConfirmationCode);
}
