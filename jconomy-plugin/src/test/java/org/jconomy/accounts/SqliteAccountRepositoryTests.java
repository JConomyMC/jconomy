package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.storage.SqliteMigrator;

class SqliteAccountRepositoryTests {

    private Connection anchor;
    private SqlConnectionFactory connectionFactory;
    private SqliteAccountRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connectionFactory = () -> DriverManager.getConnection(
                "jdbc:sqlite:file:accountrepotest?mode=memory&cache=shared");
        anchor = connectionFactory.createConnection();
        new SqliteMigrator(connectionFactory).migrate();
        repository = new SqliteAccountRepository(connectionFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var stmt = anchor.createStatement()) {
            stmt.executeUpdate("delete from account_balances");
            stmt.executeUpdate("delete from accounts");
            stmt.executeUpdate("delete from account_names");
        }
        anchor.close();
    }

    @Test
    void getByIdAndWorld_returns_empty_for_empty_database() {
        var result = repository.getByIdAndWorld(UUID.randomUUID(), "world");

        assertFalse(result.isPresent());
    }

    @Test
    void getByIdAndWorld_returns_account_with_balance() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "world", "gold", BigDecimal.valueOf(100));

        var result = repository.getByIdAndWorld(id, "world");

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(100), result.get().getBalance("gold"));
        assertEquals("world", result.get().getWorldName());
    }

    @Test
    void getAll_returns_all_accounts_across_worlds() throws Exception {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        insertAccount(id1, "world1", "gold", BigDecimal.valueOf(100));
        insertAccount(id2, "world2", "silver", BigDecimal.valueOf(50));

        var all = repository.getAll();

        assertEquals(2, all.size());
    }

    @Test
    void upsertAll_inserts_new_accounts() throws Exception {
        var id = UUID.randomUUID();
        var account = new Account(id, "world");
        account.setBalance("gold", BigDecimal.valueOf(200));

        repository.upsertAll(Set.of(account));

        try (var stmt = anchor.createStatement();
             var rs = stmt.executeQuery(
                     "select amount from account_balances where account_id='" + id + "' and world='world' and currency='gold'")) {
            assertTrue(rs.next());
            assertEquals(200.0, rs.getDouble("amount"), 0.001);
        }
    }

    @Test
    void upsertAll_with_empty_set_does_not_throw() {
        assertDoesNotThrow(() -> repository.upsertAll(Set.of()));
    }

    @Test
    void getAll_cursor_logic_returns_multiple_accounts_in_different_worlds() throws Exception {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        insertAccount(id1, "world1", "gold", BigDecimal.valueOf(10));
        insertAccount(id1, "world2", "silver", BigDecimal.valueOf(20));
        insertAccount(id2, "world1", "gold", BigDecimal.valueOf(30));

        var all = repository.getAll();

        assertEquals(3, all.size());

        var account1World1 = all.stream()
                .filter(a -> a.getAccountId().equals(id1) && a.getWorldName().equals("world1"))
                .findFirst();
        var account1World2 = all.stream()
                .filter(a -> a.getAccountId().equals(id1) && a.getWorldName().equals("world2"))
                .findFirst();
        var account2World1 = all.stream()
                .filter(a -> a.getAccountId().equals(id2) && a.getWorldName().equals("world1"))
                .findFirst();

        assertTrue(account1World1.isPresent());
        assertTrue(account1World2.isPresent());
        assertTrue(account2World1.isPresent());
        assertEquals(BigDecimal.valueOf(10), account1World1.get().getBalance("gold"));
        assertEquals(BigDecimal.valueOf(20), account1World2.get().getBalance("silver"));
        assertEquals(BigDecimal.valueOf(30), account2World1.get().getBalance("gold"));
    }

    @Test
    void deleteBalance_removes_the_specified_currency_row() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "world", "gold", BigDecimal.valueOf(100));

        repository.deleteBalance(id, "world", "gold");

        var result = repository.getByIdAndWorld(id, "world");
        assertFalse(result.isPresent());
    }

    @Test
    void deleteBalance_is_a_no_op_when_row_does_not_exist() {
        assertDoesNotThrow(() -> repository.deleteBalance(UUID.randomUUID(), "world", "gold"));
    }

    @Test
    void deleteBalance_removes_only_the_specified_currency() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "world", "gold", BigDecimal.valueOf(100));
        insertAccount(id, "world", "silver", BigDecimal.valueOf(50));

        repository.deleteBalance(id, "world", "gold");

        var result = repository.getByIdAndWorld(id, "world");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.ZERO, result.get().getBalance("gold"));
        assertEquals(BigDecimal.valueOf(50), result.get().getBalance("silver"));
    }

    private void insertAccount(UUID id, String world, String currency, BigDecimal amount) throws Exception {
        try (Statement stmt = anchor.createStatement()) {
            stmt.executeUpdate(String.format(
                    "insert into account_balances (account_id, world, currency, amount) values ('%s', '%s', '%s', %s)",
                    id, world, currency, amount.toPlainString()));
        }
    }
}
