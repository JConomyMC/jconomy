package com.jellyrekt.jconomy.storage;

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
        var url = "jdbc:sqlite:" + dataFile.toAbsolutePath();
        return DriverManager.getConnection(url);
    }
    
}
