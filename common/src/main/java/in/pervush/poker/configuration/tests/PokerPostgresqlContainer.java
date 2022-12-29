package in.pervush.poker.configuration.tests;

import org.testcontainers.containers.PostgreSQLContainer;

public class PokerPostgresqlContainer extends PostgreSQLContainer<PokerPostgresqlContainer> {

    private static final String IMAGE_VERSION = "postgres:latest";
    public static final PokerPostgresqlContainer INSTANCE = new PokerPostgresqlContainer();

    private PokerPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
    }
}
