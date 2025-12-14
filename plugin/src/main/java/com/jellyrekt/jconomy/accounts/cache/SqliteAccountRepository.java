package com.jellyrekt.jconomy.accounts.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jellyrekt.jconomy.accounts.Account;
import com.jellyrekt.jconomy.accounts.AccountCache;
import com.jellyrekt.jconomy.accounts.AccountRepository;
import com.jellyrekt.jconomy.storage.Flushable;
import com.jellyrekt.jconomy.storage.SqlConnectionFactory;

public class SqliteAccountRepository implements AccountRepository, Flushable {
    private final SqlConnectionFactory connectionFactory;
    private final AccountCache cache;

    public SqliteAccountRepository(SqlConnectionFactory connectionFactory, AccountCache cache) {
        this.connectionFactory = connectionFactory;
        this.cache = cache;
    }

    @Override
    public List<Account> getAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAll'");
    }

    @Override
    public Optional<Account> getByIdAndWorld(UUID accountId, String world) {
        return cache.get(accountId, world).or(() -> {
            var account = queryByIdAndWorld(accountId, world);
            if (account.isPresent()) {
                cache.put(account.get());
            }
            return account;
        });
    }

    private Optional<Account> queryByIdAndWorld(UUID accountId, String world) {
        var sql = """
                select a.*, n.account_name
                from accounts a
                left join account_names n
                on a.account_id = n.account_id
                where a.account_id = ? and a.world = ?
                """;
        try (
                var connection = connectionFactory.createConnection();
                var statement = connection.prepareStatement(sql);
        ) {
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
    public Optional<String> getNameByAccountId(UUID accountId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNameByAccountId'");
    }

    @Override
    public void save(Account account) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public void flush() {
        var accountSql = """
                insert into accounts (account_id, world, currency, amount)
                values (?, ?, ?, ?)
                on conflict (account_id, world, currency)
                do update set amount = excluded.amount
                """;
        var nameSql = """
                insert into account_names (account_id, account_name)
                values (?, ?)
                on conflict (account_id)
                do update set account_name = excluded.account_name
                """;
        try (
                var connection = connectionFactory.createConnection();
                var accountStatement = connection.prepareStatement(accountSql);
                var nameStatement = connection.prepareStatement(nameSql);
        ) {
            upsertAll(connection, accountStatement, nameStatement);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void upsertAll(Connection connection, PreparedStatement accountStatement, PreparedStatement nameStatement)
            throws SQLException {
        connection.setAutoCommit(false);
        for (var account : cache.getAll()) {
            try {
                upsert(account, accountStatement, nameStatement);
                connection.commit();
            } catch (SQLException ex) {
                // TODO log?
                connection.rollback();
            }
        }
    }
    
    private void upsert(Account account, PreparedStatement accountStatement, PreparedStatement nameStatement) throws SQLException {
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
}
