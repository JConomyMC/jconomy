package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JConomyDatabaseAssertionsTests {

    @TempDir
    Path tempDir;

    @Test
    void assertAccountExistsAndBalancePassForExpectedRows() throws Exception {
        Path db = tempDir.resolve("jconomy.db");
        UUID alice = UUID.nameUUIDFromBytes("OfflinePlayer:Alice".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table accounts (account_id text not null, account_name text not null, primary key(account_id))");
            statement.executeUpdate("create table account_balances (account_id text not null, world text not null, currency text not null, amount numeric, primary key(account_id, world, currency))");
            statement.executeUpdate("insert into accounts (account_id, account_name) values ('" + alice + "', 'Alice')");
            statement.executeUpdate("insert into account_balances (account_id, world, currency, amount) values ('" + alice + "', 'world', 'default', 100)");
        }

        assertDoesNotThrow(() -> JConomyDatabaseAssertions.assertAccountExists(db, "Alice"));
        assertDoesNotThrow(() -> JConomyDatabaseAssertions.assertBalance(db, "Alice", "world", "default", new BigDecimal("100")));
    }

    @Test
    void assertAccountMissingFailsWhenAccountExists() throws Exception {
        Path db = tempDir.resolve("jconomy.db");
        UUID mallory = UUID.nameUUIDFromBytes("OfflinePlayer:Mallory".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table accounts (account_id text not null, account_name text not null, primary key(account_id))");
            statement.executeUpdate("insert into accounts (account_id, account_name) values ('" + mallory + "', 'Mallory')");
        }

        assertThrows(AssertionError.class, () -> JConomyDatabaseAssertions.assertAccountMissing(db, "Mallory"));
    }

    @Test
    void assertBalanceFailsWhenAmountDiffers() throws Exception {
        Path db = tempDir.resolve("jconomy.db");
        UUID alice = UUID.nameUUIDFromBytes("OfflinePlayer:Alice".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table accounts (account_id text not null, account_name text not null, primary key(account_id))");
            statement.executeUpdate("create table account_balances (account_id text not null, world text not null, currency text not null, amount numeric, primary key(account_id, world, currency))");
            statement.executeUpdate("insert into accounts (account_id, account_name) values ('" + alice + "', 'Alice')");
            statement.executeUpdate("insert into account_balances (account_id, world, currency, amount) values ('" + alice + "', 'world', 'default', 75)");
        }

        assertThrows(
                AssertionError.class,
                () -> JConomyDatabaseAssertions.assertBalance(db, "Alice", "world", "default", new BigDecimal("100"))
        );
    }
}
