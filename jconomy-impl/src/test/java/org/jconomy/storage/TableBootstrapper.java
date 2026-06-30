package org.jconomy.storage;

import java.sql.Connection;

public final class TableBootstrapper {
    public static void bootstrapTables(Connection connection) throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                    create table if not exists accounts (
                        account_id text not null,
                        account_name text not null,
                        primary key(account_id)
                    )
                    """);
            stmt.executeUpdate("""
                    create table if not exists account_balances (
                        account_id text not null,
                        world text not null,
                        currency text not null,
                        amount numeric,
                        primary key(account_id, world, currency)
                    )
                    """);
        }
    }
}
