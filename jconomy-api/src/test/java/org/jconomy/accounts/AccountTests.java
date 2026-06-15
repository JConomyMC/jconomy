package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccountTests {

    @Test
    void getBalance_returns_zero_for_currency_with_no_balance() {
        var account = new Account(UUID.randomUUID(), "world");

        assertEquals(BigDecimal.ZERO, account.getBalance("gold"));
    }

    @Test
    void getBalance_returns_stored_balance() {
        var account = new Account(UUID.randomUUID(), "world");
        account.setBalance("gold", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), account.getBalance("gold"));
    }

    @Test
    void equals_returns_true_for_same_id_world_and_balances() {
        var id = UUID.randomUUID();
        var a = new Account(id, "world");
        a.setBalance("gold", BigDecimal.TEN);
        var b = new Account(id, "world");
        b.setBalance("gold", BigDecimal.TEN);

        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_id() {
        var a = new Account(UUID.randomUUID(), "world");
        var b = new Account(UUID.randomUUID(), "world");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_world() {
        var id = UUID.randomUUID();
        var a = new Account(id, "world1");
        var b = new Account(id, "world2");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_balance() {
        var id = UUID.randomUUID();
        var a = new Account(id, "world");
        a.setBalance("gold", BigDecimal.ONE);
        var b = new Account(id, "world");
        b.setBalance("gold", BigDecimal.TEN);

        assertNotEquals(a, b);
    }

    @Test
    void equals_is_scale_insensitive_for_balances() {
        var id = UUID.randomUUID();
        var a = new Account(id, "world");
        a.setBalance("gold", new BigDecimal("10.0"));
        var b = new Account(id, "world");
        b.setBalance("gold", new BigDecimal("10.00"));

        assertEquals(a, b);
    }

    @Test
    void hashCode_is_consistent_with_equals() {
        var id = UUID.randomUUID();
        var a = new Account(id, "world");
        a.setBalance("gold", BigDecimal.TEN);
        var b = new Account(id, "world");
        b.setBalance("gold", BigDecimal.TEN);

        assertEquals(a.hashCode(), b.hashCode());
    }
}
