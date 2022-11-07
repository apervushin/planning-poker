package in.pervush.poker.repository.postgres;

import in.pervush.poker.model.teams.DBUserTeam;
import in.pervush.poker.model.teams.MembershipStatus;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Delete;
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

public interface TeamsMapper {

    @Insert("insert into teams(team_uuid, name, create_dtm) values(#{teamUuid}, #{name}, #{createDtm})")
    void createTeam(@Param("teamUuid") UUID teamUuid,
                    @Param("name") String name,
                    @Param("createDtm") Instant createDtm);

    @Delete("""
            update teams set is_deleted = true
            where team_uuid = #{teamUuid}
                and not is_deleted
                and team_uuid = (
                    select team_uuid
                    from users_x_teams
                    where team_uuid = #{teamUuid}
                        and user_uuid = #{user}
                        and membership_status = 'OWNER'
                )
            """)
    boolean deleteTeam(@Param("teamUuid") UUID teamUuid, @Param("user") UUID userUuid);
    @Insert("""
            insert into users_x_teams(team_uuid, user_uuid, membership_status, create_dtm)
            values(#{teamUuid}, #{user}, cast(#{membershipStatus} as team_membership_status), #{createDtm})
            """)
    void addTeamMember(@Param("teamUuid") UUID teamUuid,
                       @Param("user") UUID userUuid,
                       @Param("createDtm") Instant createDtm,
                       @Param("membershipStatus") MembershipStatus membershipStatus);

    @Results(id = "userTeam")
    @ConstructorArgs(value = {
            @Arg(column = "team_uuid", javaType = UUID.class),
            @Arg(column = "team_name", javaType = String.class),
            @Arg(column = "user_uuid", javaType = UUID.class),
            @Arg(column = "create_dtm", javaType = Instant.class),
            @Arg(column = "membership_status", javaType = MembershipStatus.class),
    })
    @Select("""
            select
                tu.team_uuid,
                tu.user_uuid,
                tu.membership_status,
                tu.create_dtm,
                t.name as team_name
            from users_x_teams tu
            inner join teams t on tu.team_uuid = t.team_uuid and not t.is_deleted
            where tu.user_uuid = #{user}
            order by tu.create_dtm
            """)
    List<DBUserTeam> getUserTeamsByUserUuid(@Param("user") UUID userUuid);

    @ResultMap("userTeam")
    @Select("""
            select
                tu.team_uuid,
                tu.user_uuid,
                tu.membership_status,
                tu.create_dtm,
                t.name as team_name
            from users_x_teams tu
            inner join teams t on tu.team_uuid = t.team_uuid and not t.is_deleted
            where tu.user_uuid = #{user}
                and tu.membership_status = cast(#{membershipStatus} as team_membership_status)
            order by tu.create_dtm
            """)
    List<DBUserTeam> getUserTeamsByUserUuidAndMembershipStatus(
            @Param("user") UUID userUuid,
            @Param("membershipStatus") MembershipStatus membershipStatus
    );

    @ResultMap("userTeam")
    @Select("""
            select
                tu.team_uuid,
                tu.user_uuid,
                tu.membership_status,
                tu.create_dtm,
                t.name as team_name
            from users_x_teams tu
            inner join teams t on tu.team_uuid = t.team_uuid and not t.is_deleted
            where tu.team_uuid = #{teamUuid}
            order by tu.create_dtm
            """)
    List<DBUserTeam> getTeamMembers(@Param("teamUuid") UUID teamUuid);

    @ResultMap("userTeam")
    @Select("""
            select
                tu.team_uuid,
                tu.user_uuid,
                tu.membership_status,
                tu.create_dtm,
                t.name as team_name
            from users_x_teams tu
            inner join teams t on tu.team_uuid = t.team_uuid and not t.is_deleted
            where tu.user_uuid = #{user}
                and tu.team_uuid = #{teamUuid}
            """)
    Optional<DBUserTeam> getNotDeletedTeam(@Param("teamUuid") UUID teamUuid, @Param("user") UUID userUuid);

    @Update("""
            update users_x_teams
            set membership_status = cast(#{newMembershipStatus} as team_membership_status)
            where team_uuid = #{teamUuid}
                and user_uuid = #{user}
                and membership_status = cast(#{oldMembershipStatus} as team_membership_status)
                and team_uuid = (select team_uuid from teams where team_uuid = #{teamUuid} and not is_deleted)
            """)
    boolean setMembershipStatus(@Param("teamUuid") final UUID teamUuid,
                                @Param("user") final UUID userUuid,
                                @Param("newMembershipStatus") final MembershipStatus newMembershipStatus,
                                @Param("oldMembershipStatus") final MembershipStatus oldMembershipStatus);

    @Delete("""
            delete from users_x_teams
            where team_uuid = #{teamUuid}
                and user_uuid = #{user}
                and team_uuid = (select team_uuid from teams where team_uuid = #{teamUuid} and not is_deleted)
                and membership_status <> 'OWNER'
            """)
    boolean deleteTeamMember(@Param("teamUuid") UUID teamUuid,
                             @Param("user") UUID userUuid);
}
