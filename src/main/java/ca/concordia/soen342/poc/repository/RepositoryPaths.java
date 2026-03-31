package ca.concordia.soen342.poc.repository;

import java.nio.file.Path;

public final class RepositoryPaths {
    private RepositoryPaths() {
    }

    public static Path defaultDatabasePath() {
        return Path.of("data", "soen342-tasks.db");
    }
}
