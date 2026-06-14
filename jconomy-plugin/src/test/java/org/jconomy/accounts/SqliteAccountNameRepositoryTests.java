package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.storage.SqliteMigrator;

class SqliteAccountNameRepositoryTests {

    private Connection anchor;
    private SqlConnectionFactory connectionFactory;
    private SqliteAccountNameRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connectionFactory = () -> DriverManager.getConnection(
                "jdbc:sqlite:file:accountnamerepotest?mode=memory&cache=shared");
        anchor = connectionFactory.createConnection();
        new SqliteMigrator(connectionFactory).migrate();
        repository = new SqliteAccountNameRepository(connectionFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (var stmt = anchor.createStatement()) {
            stmt.executeUpdate("delete from account_names");
        }
        anchor.close();
    }

    @Test
    void getByAccountId_returns_empty_for_unknown_id() {
        var result = repository.getByAccountId(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void getByAccountId_returns_name_for_known_id() {
        var id = UUID.randomUUID();
        var name = new AccountName(id);
        name.setName("Steve");
        repository.upsert(name);

        var result = repository.getByAccountId(id);

        assertTrue(result.isPresent());
        assertEquals("Steve", result.get().getName());
        assertEquals(id, result.get().getAccountId());
    }

    @Test
    void getAll_returns_empty_for_empty_table() {
        var result = repository.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_returns_all_inserted_names() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var name1 = new AccountName(id1);
        name1.setName("Alice");
        var name2 = new AccountName(id2);
        name2.setName("Bob");
        repository.upsertAll(Set.of(name1, name2));

        var result = repository.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void upsert_updates_existing_name() {
        var id = UUID.randomUUID();
        var name = new AccountName(id);
        name.setName("OldName");
        repository.upsert(name);

        name.setName("NewName");
        repository.upsert(name);

        var result = repository.getByAccountId(id);
        assertTrue(result.isPresent());
        assertEquals("NewName", result.get().getName());
    }
}
