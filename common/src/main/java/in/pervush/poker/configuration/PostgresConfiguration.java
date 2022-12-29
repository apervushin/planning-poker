package in.pervush.poker.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "in.pervush.poker.repository.postgres", sqlSessionTemplateRef = "postgresSessionTemplate")
public class PostgresConfiguration {

    @Bean
    @Profile("default")
    public DataSource postgresDataSource(@Value("${postgresql.host}") final String host,
                                         @Value("${postgresql.user}") final String user,
                                         @Value("${postgresql.password}") final String password,
                                         @Value("${postgresql.database}") final String database,
                                         @Value("${postgresql.schema}") final String schema,
                                         @Value("${postgresql.ssl}") final boolean ssl,
                                         @Value("${postgresql.sslFactory}") final String sslFactory,
                                         @Value("${postgresql.sslMode}") final String sslMode) {
        final var config = new HikariConfig();
        config.setDataSourceClassName(org.postgresql.ds.PGSimpleDataSource.class.getName());
        config.setUsername(user);
        config.setPassword(password);
        config.setSchema(schema);
        config.addDataSourceProperty("databaseName", database);
        config.addDataSourceProperty("serverName", host);
        config.addDataSourceProperty("ssl", ssl);
        config.addDataSourceProperty("sslfactory", sslFactory);
        config.addDataSourceProperty("sslmode", sslMode);
        return new HikariDataSource(config);
    }

    @Bean
    public DataSourceTransactionManager transactionManager(
            @Qualifier("postgresDataSource") @Autowired final DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    @DependsOn("springLiquibase")
    public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("postgresDataSource") final DataSource ds) {
        final var sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(ds);
        sqlSessionFactory.setTypeHandlersPackage("in.pervush.poker.repository.handler");
        return sqlSessionFactory;
    }

    @Bean
    public SqlSessionTemplate postgresSessionTemplate(
            @Qualifier("sqlSessionFactory") final SqlSessionFactoryBean sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory.getObject());
    }

    @Bean
    public SpringLiquibase springLiquibase(@Qualifier("postgresDataSource") final DataSource dataSource,
                                           @Value("${liquibase.change_log}") final String changeLog) {
        final var liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        return liquibase;
    }
}
