package com.jellyrekt.jconomy.commands.transfer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferPlan;

class TransferPlanStoreTests {

    private TransferPlanStore store;
    private TransferPlan plan;

    @BeforeEach
    void setUp() {
        store = new TransferPlanStore();
        plan = new TransferPlan("my-importer", Set.of(), Set.of(), 0, ConflictPolicy.SKIP);
    }

    @Test
    void get_returns_empty_when_no_plan_stored() {
        assertTrue(store.get("alice", "my-importer").isEmpty());
    }

    @Test
    void get_returns_stored_plan() {
        store.store("alice", plan);

        assertEquals(plan, store.get("alice", "my-importer").orElseThrow());
    }

    @Test
    void get_returns_empty_for_different_sender() {
        store.store("alice", plan);

        assertTrue(store.get("bob", "my-importer").isEmpty());
    }

    @Test
    void get_returns_empty_for_different_provider() {
        store.store("alice", plan);

        assertTrue(store.get("alice", "other-importer").isEmpty());
    }

    @Test
    void invalidateAll_clears_all_stored_plans() {
        store.store("alice", plan);
        store.store("bob", new TransferPlan("other", Set.of(), Set.of(), 0, ConflictPolicy.SKIP));

        store.invalidateAll();

        assertTrue(store.get("alice", "my-importer").isEmpty());
        assertTrue(store.get("bob", "other").isEmpty());
    }

    @Test
    void store_overwrites_existing_plan_for_same_sender_and_provider() {
        store.store("alice", plan);
        var updated = new TransferPlan("my-importer", Set.of(), Set.of(), 2, ConflictPolicy.OVERWRITE);
        store.store("alice", updated);

        assertEquals(updated, store.get("alice", "my-importer").orElseThrow());
    }
}
