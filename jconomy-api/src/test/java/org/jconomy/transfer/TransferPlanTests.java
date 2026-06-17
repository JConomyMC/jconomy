package org.jconomy.transfer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jconomy.accounts.Account;

class TransferPlanTests {

    @Test
    void constructor_captures_all_fields() {
        var account = new Account(UUID.randomUUID(), "Steve");
        var accounts = Set.of(account);

        var plan = new TransferPlan("my-importer", accounts, 3, ConflictPolicy.SKIP);

        assertEquals("my-importer", plan.providerName());
        assertEquals(accounts, plan.accountsToTransfer());
        assertEquals(3, plan.conflicts());
        assertEquals(ConflictPolicy.SKIP, plan.policy());
    }

    @Test
    void constructor_accepts_empty_set() {
        var plan = new TransferPlan("provider", Set.of(), 0, ConflictPolicy.OVERWRITE);

        assertTrue(plan.accountsToTransfer().isEmpty());
        assertEquals(ConflictPolicy.OVERWRITE, plan.policy());
    }
}
