package com.jellyrekt.jconomy.transfer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.accounts.Account;
import com.jellyrekt.jconomy.accounts.AccountName;

class TransferPlanTests {

    @Test
    void constructor_captures_all_fields() {
        var account = new Account(UUID.randomUUID(), "world");
        var accountName = new AccountName(UUID.randomUUID());
        accountName.setName("Steve");
        var accounts = Set.of(account);
        var accountNames = Set.of(accountName);

        var plan = new TransferPlan("my-importer", accounts, accountNames, 3, ConflictPolicy.SKIP);

        assertEquals("my-importer", plan.providerName());
        assertEquals(accounts, plan.accountsToTransfer());
        assertEquals(accountNames, plan.accountNamesToTransfer());
        assertEquals(3, plan.conflicts());
        assertEquals(ConflictPolicy.SKIP, plan.policy());
    }

    @Test
    void constructor_accepts_empty_sets() {
        var plan = new TransferPlan("provider", Set.of(), Set.of(), 0, ConflictPolicy.OVERWRITE);

        assertTrue(plan.accountsToTransfer().isEmpty());
        assertTrue(plan.accountNamesToTransfer().isEmpty());
        assertEquals(ConflictPolicy.OVERWRITE, plan.policy());
    }
}
