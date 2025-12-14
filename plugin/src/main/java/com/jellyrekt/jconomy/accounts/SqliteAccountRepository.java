package com.jellyrekt.jconomy.accounts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.jellyrekt.jconomy.storage.SqlConnectionFactory;

public class SqliteAccountRepository implements AccountRepository {
    
    private final SqlConnectionFactory connectionFactory;
    
    public SqliteAccountRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<Account> getAll() {
        var sql = """
                select a.account_id, a.world, a.currency, a.amount, n.account_name
                from accounts a
                left join account_names n on a.account_id = n.account_id
                order by a.account_id, a.world
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
            account.setName(rs.getString("account_name"));

            do {
                account.setBalance(rs.getString("currency"), rs.getBigDecimal("amount"));
            } while (rs.next() 
                    && accountId.equals(UUID.fromString(rs.getString("account_id"))) 
                    && world.equals(rs.getString("world")));

            accounts.add(account);

            // If the inner do/while stopped before EOF, the outer do/while continues with the current row
        } while (!rs.isAfterLast());

        return accounts;
    }

    @Override
    public Optional<Account> getByIdAndWorld(UUID accountId, String world) {
        var sql = """
                select a.*, n.account_name
                from accounts a
                left join account_names n
                on a.account_id = n.account_id
                where a.account_id = ? and a.world = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var statement = connection.prepareStatement(sql);) {
            statement.setString(1, accountId.toString());
            statement.setString(2, world);
            try (var results = statement.executeQuery()) {
                return map(results);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Optional<Account> map(ResultSet result) throws Exception {
        if (!result.next()) {
            return Optional.empty();
        }
        var accountId = UUID.fromString(result.getString("account_id"));
        var worldName = result.getString("world");
        
        var account = new Account(accountId, worldName);
        account.setName(result.getString("account_name"));
        
        do {
            account.setBalance(result.getString("currency"), result.getBigDecimal("balance"));
        } while (result.next());

        return Optional.of(account);
    }

    @Override
    public void upsert(Account account) {
        upsertAll(Set.of(account));
    }

    private void upsert(Account account, PreparedStatement accountStatement, PreparedStatement nameStatement)
            throws SQLException {
        accountStatement.setString(1, account.getAccountId().toString());
        accountStatement.setString(2, account.getWorldName());

        for (var entry : account.getBalanceEntries()) {
            accountStatement.setString(3, entry.getKey());
            accountStatement.setBigDecimal(4, entry.getValue());
            accountStatement.addBatch();
        }

        nameStatement.setString(1, account.getAccountId().toString());
        nameStatement.setString(2, account.getName());

        accountStatement.executeBatch();
        nameStatement.executeUpdate();

        accountStatement.clearBatch();
    }

    @Override
    public void upsertAll(Set<Account> accounts) {
        var sqlUpsertAccount = """
                    insert into accounts (account_id, world, currency, amount)
                    values (?, ?, ?, ?)
                    on conflict (account_id, world, currency)
                    do update set amount = excluded.amount
                """;
        var sqlUpsertName = """
                    insert into account_names (account_id, account_name)
                    values (?, ?)
                    on conflict (account_id)
                    do update set account_name = excluded.account_name
                """;
        try (
                var connection = connectionFactory.createConnection();
                var accountStatement = connection.prepareStatement(sqlUpsertAccount);
                var nameStatement = connection.prepareStatement(sqlUpsertName);
        ) {
            upsertAll(accounts, connection, accountStatement, nameStatement);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void upsertAll(Set<Account> accounts, Connection connection, PreparedStatement accountStatement, PreparedStatement nameStatement)
            throws SQLException {
        connection.setAutoCommit(false);
        for (var account : accounts) {
            try {
                upsert(account, accountStatement, nameStatement);
                connection.commit();
            } catch (SQLException ex) {
                // TODO log?
                connection.rollback();
            }
        }
    }
    
}
