package org.jconomy.storage;

import java.sql.SQLException;
import java.sql.Statement;

public class SqliteMigrator {
    private final SqlConnectionFactory connectionFactory;

    public SqliteMigrator(SqlConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

                                public void migrate() {
        try (var connection = connectionFactory.createConnection()) {
            connection.setAutoCommit(false);
            try (var statement = connection.createStatement()) {
                createVersionTable(statement);
                _1_createAccountsAndAccountNames(statement);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static void createVersionTable(Statement statement) throws SQLException {
        statement.executeUpdate("create table if not exists database_meta (version integer not null)");
        try (var results = statement.executeQuery("select count(*) as row_count from database_meta")) {
            if (results.next() && results.getInt("row_count") == 0) {
                statement.executeUpdate("insert into database_meta (version) values (0)");
            }
        }
    }

    private static int getVersion(Statement statement) throws SQLException {
        try (var results = statement.executeQuery("select version from database_meta limit 1")) {
            results.next();
            return results.getInt("version");
        }
    }

    private static void _1_createAccountsAndAccountNames(Statement statement) throws SQLException {
        if (getVersion(statement) < 1) {
            statement.executeUpdate("""
                    create table if not exists accounts (
                        account_id text not null,
                        account_name text not null,
                        primary key(account_id)
                    )
                    """);
            statement.executeUpdate("""
                    create table if not exists account_balances (
                        account_id text not null,
                        world text not null,
                        currency text not null,
                        amount numeric,
                        primary key(account_id, world, currency)
                    )
                    """);
            statement.executeUpdate("update database_meta set version = 1");
        }
    }
}
