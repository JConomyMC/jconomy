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
}
