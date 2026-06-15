package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccountNameTests {

    @Test
    void equals_returns_true_for_same_id_and_name() {
        var id = UUID.randomUUID();
        var a = new AccountName(id);
        a.setName("Alice");
        var b = new AccountName(id);
        b.setName("Alice");

        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_id() {
        var a = new AccountName(UUID.randomUUID());
        a.setName("Alice");
        var b = new AccountName(UUID.randomUUID());
        b.setName("Alice");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_name() {
        var id = UUID.randomUUID();
        var a = new AccountName(id);
        a.setName("Alice");
        var b = new AccountName(id);
        b.setName("Bob");

        assertNotEquals(a, b);
    }

    @Test
    void hashCode_is_consistent_with_equals() {
        var id = UUID.randomUUID();
        var a = new AccountName(id);
        a.setName("Alice");
        var b = new AccountName(id);
        b.setName("Alice");

        assertEquals(a.hashCode(), b.hashCode());
    }
}
