package com.jellyrekt.jconomy.transfer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class TransferPreviewTests {

    @Test
    void constructor_captures_all_fields() {
        var currencies = Set.of("gold", "silver");
        var preview = new TransferPreview(10, 3, 7, 2, currencies);

        assertEquals(10, preview.totalAccounts());
        assertEquals(3, preview.newAccounts());
        assertEquals(7, preview.existingAccounts());
        assertEquals(2, preview.conflicts());
        assertEquals(currencies, preview.currenciesAffected());
    }

    @Test
    void constructor_accepts_empty_currency_set() {
        var preview = new TransferPreview(0, 0, 0, 0, Set.of());

        assertTrue(preview.currenciesAffected().isEmpty());
    }
}
