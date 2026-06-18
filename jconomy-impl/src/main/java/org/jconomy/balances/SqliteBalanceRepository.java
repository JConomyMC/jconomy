package org.jconomy.balances;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jconomy.storage.SqlConnectionFactory;

public class SqliteBalanceRepository implements BalanceRepository {

    private final SqlConnectionFactory connectionFactory;

    public SqliteBalanceRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Balance> get(UUID accountId, String worldName, String currency) {
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
                return map(rs, accountId, worldName, currency);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void upsert(Balance balance) {
        upsertAll(Set.of(balance));
    }

    @Override
    public void upsertAll(Set<Balance> balances) {
        if (balances.isEmpty()) return;
        var sql = """
                insert into account_balances (account_id, world, currency, amount)
                values (?, ?, ?, ?)
                on conflict (account_id, world, currency)
                do update set amount = excluded.amount
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            for (var balance : balances) {
                stmt.setString(1, balance.getAccountId().toString());
                stmt.setString(2, balance.getWorldName());
                stmt.setString(3, balance.getCurrency());
                stmt.setBigDecimal(4, balance.getAmount());
                stmt.addBatch();
            }
            stmt.executeBatch();
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

    private static Optional<Balance> map(ResultSet rs, UUID accountId, String worldName, String currency)
            throws SQLException {
        if (!rs.next()) return Optional.empty();
        var balance = new Balance(accountId, worldName, currency);
        BigDecimal amount = rs.getBigDecimal("amount");
        balance.setAmount(amount != null ? amount : BigDecimal.ZERO);
        return Optional.of(balance);
    }
}
