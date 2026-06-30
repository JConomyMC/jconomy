package org.jconomy.integration.spigot.harness;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

final class JConomyDatabaseAssertions {

    private JConomyDatabaseAssertions() {
    }

    static void assertAccountExists(Path databasePath, String accountName) {
        UUID accountId = offlineUuid(accountName);
        String sql = "select count(*) as count from accounts where account_id = ? and account_name = ?";

        int count = queryCount(databasePath, sql, statement -> {
            statement.setString(1, accountId.toString());
            statement.setString(2, accountName);
        });

        if (count != 1) {
            throw new AssertionError("Expected account to exist: " + accountName);
        }
    }

    static void assertAccountMissing(Path databasePath, String accountName) {
        UUID accountId = offlineUuid(accountName);
        String sql = "select count(*) as count from accounts where account_id = ? and account_name = ?";

        int count = queryCount(databasePath, sql, statement -> {
            statement.setString(1, accountId.toString());
            statement.setString(2, accountName);
        });

        if (count != 0) {
            throw new AssertionError("Expected account to be missing: " + accountName);
        }
    }

    static void assertBalance(
            Path databasePath,
            String accountName,
            String world,
            String currency,
            BigDecimal expectedAmount
    ) {
        UUID accountId = offlineUuid(accountName);
        String sql = "select amount from account_balances where account_id = ? and world = ? and currency = ?";

        BigDecimal actual = queryAmount(databasePath, sql, statement -> {
            statement.setString(1, accountId.toString());
            statement.setString(2, world);
            statement.setString(3, currency);
        });

        if (actual == null) {
            throw new AssertionError("Expected balance row to exist for account: " + accountName);
        }

        if (actual.compareTo(expectedAmount) != 0) {
            throw new AssertionError(
                    "Expected balance " + expectedAmount + " but found " + actual + " for account " + accountName
            );
        }
    }

    private static int queryCount(Path databasePath, String sql, StatementConfigurer configurer) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            configurer.configure(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count");
                }
                return 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query database: " + databasePath, exception);
        }
    }

    private static BigDecimal queryAmount(Path databasePath, String sql, StatementConfigurer configurer) {
        try (
                Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            configurer.configure(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getBigDecimal("amount");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to query database: " + databasePath, exception);
        }
    }

    private static UUID offlineUuid(String accountName) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + accountName).getBytes(StandardCharsets.UTF_8));
    }

    @FunctionalInterface
    private interface StatementConfigurer {

        void configure(PreparedStatement statement) throws SQLException;
    }
}
