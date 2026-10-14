package org.jconomy.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnectionFactory implements SqlConnectionFactory {
    private final Path dataFile;

    public SqliteConnectionFactory(Path datafile) {
        this.dataFile = datafile;
    }

    @Override
    public Connection createConnection() throws SQLException {
        var path = dataFile.toAbsolutePath();
        createParentDirectories(path);
        return DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    private static void createParentDirectories(Path path) throws SQLException {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ex) {
            throw new SQLException("Could not create directory for database file " + path, ex);
        }
    }

}
