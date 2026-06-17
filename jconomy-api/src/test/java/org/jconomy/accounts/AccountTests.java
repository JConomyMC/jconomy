package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccountTests {

    @Test
    void getAccountId_returns_constructor_value() {
        var id = UUID.randomUUID();
        var account = new Account(id, "Alice");

        assertEquals(id, account.getAccountId());
    }

    @Test
    void getName_returns_constructor_value() {
        var account = new Account(UUID.randomUUID(), "Alice");

        assertEquals("Alice", account.getName());
    }

    @Test
    void setName_updates_name() {
        var account = new Account(UUID.randomUUID(), "Alice");
        account.setName("Bob");

        assertEquals("Bob", account.getName());
    }

    @Test
    void equals_returns_true_for_same_id_and_name() {
        var id = UUID.randomUUID();
        var a = new Account(id, "Alice");
        var b = new Account(id, "Alice");

        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_id() {
        var a = new Account(UUID.randomUUID(), "Alice");
        var b = new Account(UUID.randomUUID(), "Alice");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_name() {
        var id = UUID.randomUUID();
        var a = new Account(id, "Alice");
        var b = new Account(id, "Bob");

        assertNotEquals(a, b);
    }

    @Test
    void hashCode_is_consistent_with_equals() {
        var id = UUID.randomUUID();
        var a = new Account(id, "Alice");
        var b = new Account(id, "Alice");

        assertEquals(a.hashCode(), b.hashCode());
    }
}
