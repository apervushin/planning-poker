package in.pervush.poker.configuration.tests;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.pervush.poker.configuration.PostgresConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Import(PostgresConfiguration.class)
public class TestPostgresConfiguration {

    @Bean
    @Profile("test")
    public DataSource postgresDataSource() {
        final var containerInstance = PokerPostgresqlContainer.INSTANCE;
        containerInstance.start();

        final var config = new HikariConfig();
        config.setDataSourceClassName(org.postgresql.ds.PGSimpleDataSource.class.getName());
        config.setUsername(containerInstance.getUsername());
        config.setPassword(containerInstance.getPassword());
        config.setSchema("public");
        config.addDataSourceProperty("databaseName", containerInstance.getDatabaseName());
        config.addDataSourceProperty("serverName", containerInstance.getHost() + ":" + containerInstance.getFirstMappedPort());
        return new HikariDataSource(config);
    }

}
