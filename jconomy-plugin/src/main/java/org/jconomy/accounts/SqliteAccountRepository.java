package org.jconomy.accounts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jconomy.storage.SqlConnectionFactory;

public class SqliteAccountRepository implements AccountRepository {

    private final SqlConnectionFactory connectionFactory;

    public SqliteAccountRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Account> getAccount(UUID accountId) {
        var sql = "select account_id, account_name from accounts where account_id = ?";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            try (var rs = stmt.executeQuery()) {
                return mapOne(rs);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        var sql = "select account_id, account_name from accounts";
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql);
                var rs = stmt.executeQuery()) {
            return mapAll(rs);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void upsert(Account account) {
        var sql = """
                insert into accounts (account_id, account_name)
                values (?, ?)
                on conflict (account_id) do update set account_name = excluded.account_name
                """;
        try (
                var connection = connectionFactory.createConnection();
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, account.getAccountId().toString());
            stmt.setString(2, account.getName());
            stmt.executeUpdate();
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
                var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId.toString());
            stmt.setString(2, name);
            return stmt.executeUpdate() > 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deleteAccount(UUID accountId) {
        var deleteBalances = "delete from account_balances where account_id = ?";
        var deleteAccount = "delete from accounts where account_id = ?";
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

    private static Optional<Account> mapOne(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        return Optional.of(new Account(
                UUID.fromString(rs.getString("account_id")),
                rs.getString("account_name")));
    }

    private static List<Account> mapAll(ResultSet rs) throws SQLException {
        var accounts = new ArrayList<Account>();
        while (rs.next()) {
            accounts.add(new Account(
                    UUID.fromString(rs.getString("account_id")),
                    rs.getString("account_name")));
        }
        return accounts;
    }
}

