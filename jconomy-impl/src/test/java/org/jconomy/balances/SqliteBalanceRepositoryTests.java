package org.jconomy.balances;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
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
    void get_returns_stored_amount() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(150));

        var result = repository.get(id, "world", "gold");

        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(150), result.get());
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

    // --- assignAll ---

    @Test
    void assignAll_overwrites_existing_amount() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        repository.assignAll(List.of(new BalanceAssignment(id, "world", "gold", BigDecimal.valueOf(40))));

        assertEquals(0, BigDecimal.valueOf(40).compareTo(repository.get(id, "world", "gold").get()));
    }

    @Test
    void assignAll_inserts_amount_when_no_row_exists() {
        var id = UUID.randomUUID();

        repository.assignAll(List.of(new BalanceAssignment(id, "world", "gold", BigDecimal.valueOf(40))));

        var result = repository.get(id, "world", "gold");
        assertTrue(result.isPresent());
        assertEquals(0, BigDecimal.valueOf(40).compareTo(result.get()));
    }

    // --- adjustAll ---

    @Test
    void adjustAll_adds_delta_to_existing_amount() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        repository.adjustAll(List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))));

        assertEquals(0, BigDecimal.valueOf(110).compareTo(repository.get(id, "world", "gold").get()));
    }

    @Test
    void adjustAll_inserts_delta_when_no_row_exists() {
        var id = UUID.randomUUID();

        repository.adjustAll(List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(25))));

        var result = repository.get(id, "world", "gold");
        assertTrue(result.isPresent());
        assertEquals(0, BigDecimal.valueOf(25).compareTo(result.get()));
    }

    @Test
    void adjustAll_applies_successive_adjustments_cumulatively() throws Exception {
        var id = UUID.randomUUID();
        insert(id, "world", "gold", BigDecimal.valueOf(100));

        repository.adjustAll(List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))));
        repository.adjustAll(List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(20))));

        assertEquals(0, BigDecimal.valueOf(130).compareTo(repository.get(id, "world", "gold").get()));
    }

    @Test
    void adjustAll_failure_mid_batch_commits_nothing() throws Exception {
        var existingId = UUID.randomUUID();
        var newId = UUID.randomUUID();
        insert(existingId, "world", "gold", BigDecimal.valueOf(100));
        failInsertsForCurrency("boom");

        assertThrows(RuntimeException.class, () -> repository.adjustAll(List.of(
                new BalanceAdjustment(existingId, "world", "gold", BigDecimal.valueOf(10)),
                new BalanceAdjustment(newId, "world", "gold", BigDecimal.valueOf(25)),
                new BalanceAdjustment(newId, "world", "boom", BigDecimal.ONE))));

        assertEquals(0, BigDecimal.valueOf(100).compareTo(repository.get(existingId, "world", "gold").get()));
        assertFalse(repository.get(newId, "world", "gold").isPresent());
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

    private void failInsertsForCurrency(String currency) throws Exception {
        try (Statement stmt = anchor.createStatement()) {
            stmt.executeUpdate(String.format("""
                    create trigger fail_%1$s before insert on account_balances
                    when new.currency = '%1$s'
                    begin select raise(abort, '%1$s'); end
                    """, currency));
        }
    }
}
