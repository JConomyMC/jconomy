package org.jconomy.storage;

import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class SqliteMigratorTests {

    private Connection anchor;
    private SqliteMigrator migrator;

    @BeforeEach
    void setUp() throws Exception {
        SqlConnectionFactory connectionFactory = () -> {
            return DriverManager.getConnection("jdbc:sqlite:file:testdb?mode=memory&cache=shared");
        };
        migrator = new SqliteMigrator(connectionFactory);
        anchor = connectionFactory.createConnection();
    }

    @AfterEach
    void tearDown() throws Exception {
        anchor.close();
    }

    @Test
    void migrate_creates_schema_and_sets_version() throws Exception {
        migrator.migrate();

        try (Statement stmt = anchor.createStatement()) {
            // database_meta exists
            try (ResultSet rs = stmt.executeQuery(
                    "select version from database_meta")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("version"));
                assertFalse(rs.next()); // exactly one row
            }

            assertTrue(tableExists(stmt, "accounts"));
            assertTrue(tableExists(stmt, "account_balances"));
        }
    }

    @Test
    void migrate_is_idempotent() throws Exception {
        migrator.migrate();
        migrator.migrate(); // should not throw

        try (Statement stmt = anchor.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "select version from database_meta")) {
            rs.next();
            assertEquals(1, rs.getInt("version"));
        }
    }

    private static boolean tableExists(Statement stmt, String table)
            throws SQLException {
        try (ResultSet rs = stmt.executeQuery(
                "select name from sqlite_master where type='table' and name='" + table + "'")) {
            return rs.next();
        }
    }
}
