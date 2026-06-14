package org.jconomy.accounts;

import java.lang.StackWalker.Option;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jconomy.storage.SqlConnectionFactory;

public class SqliteAccountNameRepository implements AccountNameRepository {
    private final SqlConnectionFactory connectionFactory;

    public SqliteAccountNameRepository(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public List<AccountName> getAll() {
        try (
            var connection = connectionFactory.createConnection();
            var statement = connection.prepareStatement("select * from account_names");
            var result = statement.executeQuery();
        ) {
            var accountNames = new ArrayList<AccountName>();
            while (result.next()) {
                var accountId = UUID.fromString(result.getString("account_id"));
                var accountName = new AccountName(accountId);
                accountName.setName(result.getString("account_name"));
                accountNames.add(accountName);
            }
            return accountNames;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<AccountName> getByAccountId(UUID accountId) {
        try (
            var connection = connectionFactory.createConnection();
            var statement = connection.prepareStatement("select * from account_names where account_id = ?");
        ) {
            statement.setString(1, accountId.toString());
            var result = statement.executeQuery();
            if (!result.next()) {
                return Optional.ofNullable(null);
            }
            var accountName = new AccountName(UUID.fromString(result.getString("account_id")));
            accountName.setName(result.getString("account_name"));
            return Optional.of(accountName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void upsert(AccountName accountName) {
        upsertAll(Set.of(accountName));
    }

    @Override
    public void upsertAll(Set<AccountName> accountNames) {
        var sql = """
            insert into account_names (account_id, account_name)
            values (?, ?)
            on conflict (account_id)
            do update set account_name = excluded.account_name
        """;

        try (
            var connection = connectionFactory.createConnection();
            var stmt = connection.prepareStatement(sql);
        ) {
            for (var name : accountNames) {
                stmt.setString(1, name.getAccountId().toString());
                stmt.setString(2, name.getName());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
