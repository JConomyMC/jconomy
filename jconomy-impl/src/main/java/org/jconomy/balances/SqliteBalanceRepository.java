package org.jconomy.balances;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.jconomy.storage.SqlConnectionFactory;

public class SqliteBalanceRepository implements BalanceRepository {

    private final SqlConnectionFactory connectionFactory;

    public SqliteBalanceRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<BigDecimal> get(UUID accountId, String worldName, String currency) {
        var sql = """
                select amount from account_balances
                where account_id = ? and world = ? and currency = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            stmt.setString(2, worldName);
            stmt.setString(3, currency);
            try (var rs = stmt.executeQuery()) {
                return readAmount(rs);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void assignAll(Collection<BalanceAssignment> assignments) {
        var sql = """
                insert into account_balances (account_id, world, currency, amount)
                values (?, ?, ?, ?)
                on conflict (account_id, world, currency)
                do update set amount = excluded.amount
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            for (var assignment : assignments) {
                stmt.setString(1, assignment.accountId().toString());
                stmt.setString(2, assignment.worldName());
                stmt.setString(3, assignment.currency());
                stmt.setBigDecimal(4, assignment.amount());
                stmt.addBatch();
            }
            executeBatchAtomically(connection, stmt);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void adjustAll(Collection<BalanceAdjustment> adjustments) {
        var sql = """
                insert into account_balances (account_id, world, currency, amount)
                values (?, ?, ?, ?)
                on conflict (account_id, world, currency)
                do update set amount = amount + excluded.amount
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            for (var adjustment : adjustments) {
                stmt.setString(1, adjustment.accountId().toString());
                stmt.setString(2, adjustment.worldName());
                stmt.setString(3, adjustment.currency());
                stmt.setBigDecimal(4, adjustment.delta());
                stmt.addBatch();
            }
            executeBatchAtomically(connection, stmt);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(UUID accountId, String worldName, String currency) {
        var sql = """
                delete from account_balances
                where account_id = ? and world = ? and currency = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            stmt.setString(2, worldName);
            stmt.setString(3, currency);
            stmt.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deleteByAccount(UUID accountId) {
        var sql = "delete from account_balances where account_id = ?";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            stmt.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void executeBatchAtomically(Connection connection, PreparedStatement stmt) throws SQLException {
        connection.setAutoCommit(false);
        try {
            stmt.executeBatch();
            connection.commit();
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                ex.addSuppressed(rollbackFailure);
            }
            throw ex;
        }
    }

    private static Optional<BigDecimal> readAmount(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        BigDecimal amount = rs.getBigDecimal("amount");
        return Optional.of(amount != null ? amount : BigDecimal.ZERO);
    }
}
