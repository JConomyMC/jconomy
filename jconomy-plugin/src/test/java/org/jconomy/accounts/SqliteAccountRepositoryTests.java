package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
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
        }
        anchor.close();
    }

    // --- getAccount ---

    @Test
    void getAccount_returns_empty_for_unknown_id() {
        assertFalse(repository.getAccount(UUID.randomUUID()).isPresent());
    }

    @Test
    void getAccount_returns_account_with_correct_name() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Alice");

        var result = repository.getAccount(id);

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
        assertEquals(id, result.get().getAccountId());
    }

    // --- getAllAccounts ---

    @Test
    void getAllAccounts_returns_empty_list_for_empty_database() {
        assertTrue(repository.getAllAccounts().isEmpty());
    }

    @Test
    void getAllAccounts_returns_all_rows() throws Exception {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        insertAccount(id1, "Alice");
        insertAccount(id2, "Bob");

        var all = repository.getAllAccounts();

        assertEquals(2, all.size());
    }

    // --- upsert ---

    @Test
    void upsert_inserts_new_account() {
        var id = UUID.randomUUID();
        repository.upsert(new Account(id, "Alice"));

        assertTrue(accountEntityExists(id));
    }

    @Test
    void upsert_updates_name_for_existing_account() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Alice");

        repository.upsert(new Account(id, "Bob"));

        assertEquals("Bob", repository.getAccount(id).get().getName());
    }

    // --- createAccount ---

    @Test
    void createAccount_inserts_account_row() {
        var id = UUID.randomUUID();

        repository.createAccount(id, "Player");

        assertTrue(accountEntityExists(id));
    }

    @Test
    void createAccount_returns_true_for_new_account() {
        assertTrue(repository.createAccount(UUID.randomUUID(), "Player"));
    }

    @Test
    void createAccount_returns_false_for_existing_account() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Player");

        assertFalse(repository.createAccount(id, "Player"));
    }

    @Test
    void createAccount_is_idempotent() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Player");

        assertDoesNotThrow(() -> repository.createAccount(id, "Player"));
        assertTrue(accountEntityExists(id));
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_removes_account_row() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Player");

        repository.deleteAccount(id);

        assertFalse(accountEntityExists(id));
    }

    @Test
    void deleteAccount_removes_associated_balance_rows() throws Exception {
        var id = UUID.randomUUID();
        insertAccount(id, "Player");
        insertBalance(id, "world", "gold");

        repository.deleteAccount(id);

        assertFalse(balanceExists(id, "world", "gold"));
    }

    @Test
    void deleteAccount_is_a_no_op_when_account_does_not_exist() {
        assertDoesNotThrow(() -> repository.deleteAccount(UUID.randomUUID()));
    }

    // --- helpers ---

    private void insertAccount(UUID id, String name) throws Exception {
        try (Statement stmt = anchor.createStatement()) {
            stmt.executeUpdate(String.format(
                    "insert into accounts (account_id, account_name) values ('%s', '%s')",
                    id, name));
        }
    }

    private void insertBalance(UUID id, String world, String currency) throws Exception {
        try (Statement stmt = anchor.createStatement()) {
            stmt.executeUpdate(String.format(
                    "insert into account_balances (account_id, world, currency, amount) values ('%s', '%s', '%s', 0)",
                    id, world, currency));
        }
    }

    private boolean accountEntityExists(UUID id) {
        try (var stmt = anchor.createStatement();
             var rs = stmt.executeQuery(String.format(
                     "select count(*) as c from accounts where account_id='%s'", id))) {
            return rs.next() && rs.getInt("c") > 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean balanceExists(UUID id, String world, String currency) {
        try (var stmt = anchor.createStatement();
             var rs = stmt.executeQuery(String.format(
                     "select count(*) as c from account_balances where account_id='%s' and world='%s' and currency='%s'",
                     id, world, currency))) {
            return rs.next() && rs.getInt("c") > 0;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
