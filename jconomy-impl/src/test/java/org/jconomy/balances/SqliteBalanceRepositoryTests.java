package org.jconomy.balances;

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
import org.jconomy.storage.TableBootstrapper;

class SqliteBalanceRepositoryTests {

    private Connection anchor;
    private SqlConnectionFactory connectionFactory;
    private SqliteBalanceRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connectionFactory = () -> DriverManager.getConnection(
                "jdbc:sqlite:file:balancerepotest?mode=memory&cache=shared");
        anchor = connectionFactory.createConnection();
        TableBootstrapper.bootstrapTables(anchor);
        repository = new SqliteBalanceRepository(connectionFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var stmt = anchor.createStatement()) {
            stmt.executeUpdate("delete from account_balances");
        }
        anchor.close();
    }

    // --- get ---

    @Test
    void get_returns_empty_when_no_row_exists() {
        var result = repository.get(UUID.randomUUID(), "world", "gold");

        assertFalse(result.isPresent());
    }

    @Test
    void get_returns_balance_with_stored_amount() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(150));

        var result = repository.get(id, "world", "gold");

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(150), result.get().getAmount());
    }

    @Test
    void get_returns_empty_for_different_world() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        var result = repository.get(id, "nether", "gold");

        assertFalse(result.isPresent());
    }

    @Test
    void get_returns_empty_for_different_currency() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        var result = repository.get(id, "world", "silver");

        assertFalse(result.isPresent());
    }

    // --- upsert ---

    @Test
    void upsert_inserts_new_row() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(50));

        repository.upsert(balance);

        var result = repository.get(id, "world", "gold");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(50), result.get().getAmount());
    }

    @Test
    void upsert_updates_existing_row() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(50));

        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(200));
        repository.upsert(balance);

        var result = repository.get(id, "world", "gold");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(200), result.get().getAmount());
    }

    // --- upsertAll ---

    @Test
    void upsertAll_inserts_multiple_rows() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var b1 = new Balance(id1, "world", "gold");
        b1.setAmount(BigDecimal.valueOf(10));
        var b2 = new Balance(id2, "world", "silver");
        b2.setAmount(BigDecimal.valueOf(20));

        repository.upsertAll(Set.of(b1, b2));

        assertEquals(BigDecimal.valueOf(10), repository.get(id1, "world", "gold").get().getAmount());
        assertEquals(BigDecimal.valueOf(20), repository.get(id2, "world", "silver").get().getAmount());
    }

    @Test
    void upsertAll_with_empty_set_is_a_no_op() {
        assertDoesNotThrow(() -> repository.upsertAll(Set.of()));
    }

    // --- delete ---

    @Test
    void delete_removes_the_matching_row() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        repository.delete(id, "world", "gold");

        assertFalse(repository.get(id, "world", "gold").isPresent());
    }

    @Test
    void delete_is_a_no_op_when_row_does_not_exist() {
        assertDoesNotThrow(() -> repository.delete(UUID.randomUUID(), "world", "gold"));
    }

    @Test
    void delete_does_not_affect_other_rows() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));
        insert(id, "world", "silver", BigDecimal.valueOf(50));

        repository.delete(id, "world", "gold");

        assertTrue(repository.get(id, "world", "silver").isPresent());
    }

    // --- deleteByAccount ---

    @Test
    void deleteByAccount_removes_all_rows_for_account() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));
        insert(id, "nether", "gold", BigDecimal.valueOf(50));

        repository.deleteByAccount(id);

        assertFalse(repository.get(id, "world", "gold").isPresent());
        assertFalse(repository.get(id, "nether", "gold").isPresent());
    }

    @Test
    void deleteByAccount_does_not_affect_other_accounts() throws Exception {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        insert(id1, "world", "gold", BigDecimal.valueOf(100));
        insert(id2, "world", "gold", BigDecimal.valueOf(200));

        repository.deleteByAccount(id1);

        assertTrue(repository.get(id2, "world", "gold").isPresent());
    }

    @Test
    void deleteByAccount_is_a_no_op_when_account_has_no_rows() {
        assertDoesNotThrow(() -> repository.deleteByAccount(UUID.randomUUID()));
    }

    // --- helper ---

    private void insert(UUID id, String world, String currency, BigDecimal amount) throws Exception {
        try (Statement stmt = anchor.createStatement()) {
            stmt.executeUpdate(String.format(
                    "insert into account_balances (account_id, world, currency, amount) values ('%s', '%s', '%s', %s)",
                    id, world, currency, amount.toPlainString()));
        }
    }
}
