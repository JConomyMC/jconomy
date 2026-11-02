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

    private static final String SELECT_AMOUNT_SQL = """
            select amount from account_balances
            where account_id = ? and world = ? and currency = ?
            """;

    private static final String ASSIGN_SQL = """
            insert into account_balances (account_id, world, currency, amount)
            values (?, ?, ?, ?)
            on conflict (account_id, world, currency)
            do update set amount = excluded.amount
            """;

    @Override
    public Optional<BigDecimal> get(UUID accountId, String worldName, String currency) {
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(SELECT_AMOUNT_SQL)) {
            return selectAmount(stmt, accountId, worldName, currency);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void assignAll(Collection<BalanceAssignment> assignments) {
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(ASSIGN_SQL)) {
            for (var assignment : assignments) {
                bindAssignment(stmt, assignment.accountId(), assignment.worldName(), assignment.currency(),
                        assignment.amount());
                stmt.addBatch();
            }
            runAtomically(connection, stmt::executeBatch);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void adjustAll(Collection<BalanceAdjustment> adjustments) {
        try (
                var connection = connectionFactory.createConnection();
                var select = connection.prepareStatement(SELECT_AMOUNT_SQL);
                var assign = connection.prepareStatement(ASSIGN_SQL)) {
            runAtomically(connection, () -> {
                for (var adjustment : adjustments) {
                    var current = selectAmount(select, adjustment.accountId(), adjustment.worldName(),
                            adjustment.currency()).orElse(BigDecimal.ZERO);
                    bindAssignment(assign, adjustment.accountId(), adjustment.worldName(), adjustment.currency(),
                            current.add(adjustment.delta()));
                    assign.executeUpdate();
                }
            });
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

    private interface SqlWork {
        void run() throws SQLException;
    }

    private static void runAtomically(Connection connection, SqlWork work) throws SQLException {
        connection.setAutoCommit(false);
        try {
            work.run();
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

    private static Optional<BigDecimal> selectAmount(
            PreparedStatement stmt, UUID accountId, String worldName, String currency) throws SQLException {
        stmt.setString(1, accountId.toString());
        stmt.setString(2, worldName);
        stmt.setString(3, currency);
        try (var rs = stmt.executeQuery()) {
            return readAmount(rs);
        }
    }

    private static void bindAssignment(
            PreparedStatement stmt, UUID accountId, String worldName, String currency, BigDecimal amount)
            throws SQLException {
        stmt.setString(1, accountId.toString());
        stmt.setString(2, worldName);
        stmt.setString(3, currency);
        stmt.setBigDecimal(4, amount);
    }

    private static Optional<BigDecimal> readAmount(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        BigDecimal amount = rs.getBigDecimal("amount");
        return Optional.of(amount != null ? amount : BigDecimal.ZERO);
    }
}
