package org.jconomy.storage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteConnectionFactoryTests {

    @TempDir
    Path tempDir;

    @Test
    void createConnection_creates_database_file_when_missing() throws Exception {
        var dataFile = tempDir.resolve("jconomy.db");

        new SqliteConnectionFactory(dataFile).createConnection().close();

        assertTrue(Files.exists(dataFile));
    }

    @Test
    void createConnection_creates_missing_parent_directories() throws Exception {
        var dataFile = tempDir.resolve("missing").resolve("jconomy.db");

        new SqliteConnectionFactory(dataFile).createConnection().close();

        assertTrue(Files.exists(dataFile));
    }
}
