package org.jconomy.accounts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jconomy.storage.SqlConnectionFactory;

public class SqliteAccountRepository implements AccountRepository {
    
    private final SqlConnectionFactory connectionFactory;
    
    public SqliteAccountRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Account> getAll() {
        var sql = """
                select a.account_id, ab.world, ab.currency, ab.amount
                from accounts a
                join account_balances ab
                    on a.account_id = ab.account_id
                order by a.account_id, ab.world
                """;

        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql);
                var rs = stmt.executeQuery();) {
            return mapAll(rs);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<Account> mapAll(ResultSet rs) throws SQLException {
        var accounts = new ArrayList<Account>();

        if (!rs.next()) {
            return accounts;
        }

        do {
            UUID accountId = UUID.fromString(rs.getString("account_id"));
            String world = rs.getString("world");
            var account = new Account(accountId, world);

            do {
                String currency = rs.getString("currency");
                if (currency != null) {
                    account.setBalance(currency, rs.getBigDecimal("amount"));
                }
            } while (rs.next()
                    && accountId.equals(UUID.fromString(rs.getString("account_id")))
                    && world.equals(rs.getString("world")));

            accounts.add(account);
        } while (!rs.isAfterLast());

        return accounts;
    }

    @Override
    public Optional<Account> getByIdAndWorld(UUID accountId, String world) {
        var sql = """
                select a.account_id, ab.currency, ab.amount
                from accounts a
                left join account_balances ab
                    on a.account_id = ab.account_id and ab.world = ?
                where a.account_id = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var statement = connection.prepareStatement(sql);) {
            statement.setString(1, world);
            statement.setString(2, accountId.toString());
            try (var results = statement.executeQuery()) {
                return map(results, accountId, world);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Optional<Account> map(ResultSet result, UUID accountId, String world) throws Exception {
        if (!result.next()) {
            return Optional.empty();
        }

        var account = new Account(accountId, world);

        do {
            String currency = result.getString("currency");
            if (currency != null) {
                account.setBalance(currency, result.getBigDecimal("amount"));
            }
        } while (result.next());

        return Optional.of(account);
    }

    @Override
    public void upsert(Account account) {
        upsertAll(Set.of(account));
    }

    @Override
    public void upsertAll(Set<Account> accounts) {
        var sqlUpsertAccount = """
                    insert into account_balances (account_id, world, currency, amount)
                    values (?, ?, ?, ?)
                    on conflict (account_id, world, currency)
                    do update set amount = excluded.amount
                """;
        try (
                var connection = connectionFactory.createConnection();
                var accountStatement = connection.prepareStatement(sqlUpsertAccount);
        ) {
            upsertAll(accounts, connection, accountStatement);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void upsertAll(Set<Account> accounts, Connection connection, PreparedStatement accountStatement)
            throws SQLException {
        connection.setAutoCommit(false);
        for (var account : accounts) {
            try {
                upsert(account, accountStatement);
                connection.commit();
            } catch (SQLException ex) {
                // TODO log?
                connection.rollback();
            }
        }
    }

    private void upsert(Account account, PreparedStatement accountStatement) throws SQLException {
        accountStatement.setString(1, account.getAccountId().toString());
        accountStatement.setString(2, account.getWorldName());

        for (var entry : account.getBalanceEntries()) {
            accountStatement.setString(3, entry.getKey());
            accountStatement.setBigDecimal(4, entry.getValue());
            accountStatement.addBatch();
        }

        accountStatement.executeBatch();
        accountStatement.clearBatch();
    }

    @Override
    public void deleteBalance(UUID accountId, String world, String currency) {
        var sql = """
                delete from account_balances
                where account_id = ? and world = ? and currency = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId.toString());
            statement.setString(2, world);
            statement.setString(3, currency);
            statement.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean createAccount(UUID accountId, String name) {
        var sql = """
                insert into accounts (account_id, account_name)
                values (?, ?)
                on conflict (account_id) do nothing
                """;
        try (
                var connection = connectionFactory.createConnection();
                var statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId.toString());
            statement.setString(2, name);
            return statement.executeUpdate() > 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deleteAccount(UUID accountId) {
        var deleteBalances = """
                delete from account_balances
                where account_id = ?
                """;
        var deleteAccount = """
                delete from accounts
                where account_id = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var balancesStmt = connection.prepareStatement(deleteBalances);
                var accountStmt = connection.prepareStatement(deleteAccount)) {
            connection.setAutoCommit(false);
            try {
                balancesStmt.setString(1, accountId.toString());
                balancesStmt.executeUpdate();
                accountStmt.setString(1, accountId.toString());
                accountStmt.executeUpdate();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Map<UUID, String> getAllAccountNames() {
        var sql = "select account_id, account_name from accounts";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql);
                var rs = stmt.executeQuery()) {
            var result = new HashMap<UUID, String>();
            while (rs.next()) {
                result.put(UUID.fromString(rs.getString("account_id")), rs.getString("account_name"));
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<String> getAccountName(UUID accountId) {
        var sql = "select account_name from accounts where account_id = ?";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            try (var rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("account_name")) : Optional.empty();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean renameAccount(UUID accountId, String name) {
        var sql = "update accounts set account_name = ? where account_id = ?";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, accountId.toString());
            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
