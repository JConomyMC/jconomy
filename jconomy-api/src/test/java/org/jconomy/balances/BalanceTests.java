package org.jconomy.balances;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BalanceTests {

    @Test
    void getAmount_returns_zero_by_default() {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");

        assertEquals(BigDecimal.ZERO, balance.getAmount());
    }

    @Test
    void getAmount_returns_set_value() {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), balance.getAmount());
    }

    @Test
    void getAccountId_returns_constructor_value() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");

        assertEquals(id, balance.getAccountId());
    }

    @Test
    void getWorldName_returns_constructor_value() {
        var balance = new Balance(UUID.randomUUID(), "nether", "gold");

        assertEquals("nether", balance.getWorldName());
    }

    @Test
    void getCurrency_returns_constructor_value() {
        var balance = new Balance(UUID.randomUUID(), "world", "silver");

        assertEquals("silver", balance.getCurrency());
    }

    @Test
    void equals_returns_true_for_same_key_regardless_of_amount() {
        var id = UUID.randomUUID();
        var a = new Balance(id, "world", "gold");
        a.setAmount(BigDecimal.ONE);
        var b = new Balance(id, "world", "gold");
        b.setAmount(BigDecimal.TEN);

        assertEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_account_id() {
        var a = new Balance(UUID.randomUUID(), "world", "gold");
        var b = new Balance(UUID.randomUUID(), "world", "gold");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_world() {
        var id = UUID.randomUUID();
        var a = new Balance(id, "world1", "gold");
        var b = new Balance(id, "world2", "gold");

        assertNotEquals(a, b);
    }

    @Test
    void equals_returns_false_for_different_currency() {
        var id = UUID.randomUUID();
        var a = new Balance(id, "world", "gold");
        var b = new Balance(id, "world", "silver");

        assertNotEquals(a, b);
    }

    @Test
    void hashCode_is_consistent_with_equals() {
        var id = UUID.randomUUID();
        var a = new Balance(id, "world", "gold");
        var b = new Balance(id, "world", "gold");

        assertEquals(a.hashCode(), b.hashCode());
    }
}
