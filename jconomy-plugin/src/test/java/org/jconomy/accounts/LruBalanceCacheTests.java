package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jconomy.config.CacheConfig;

class LruBalanceCacheTests {

    private static LruBalanceCache cacheWithLimit(int limit) {
        var config = mock(CacheConfig.class);
        when(config.getLruLimit()).thenReturn(limit);
        return new LruBalanceCache(config);
    }

    @Test
    void get_returns_empty_for_unknown_balance() {
        var cache = cacheWithLimit(10);

        assertFalse(cache.get(UUID.randomUUID(), "world", "gold").isPresent());
    }

    @Test
    void get_returns_balance_after_put() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(50));

        cache.put(balance);

        var result = cache.get(id, "world", "gold");
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(50), result.get().getAmount());
    }

    @Test
    void get_returns_empty_for_different_world() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));

        assertFalse(cache.get(id, "nether", "gold").isPresent());
    }

    @Test
    void get_returns_empty_for_different_currency() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));

        assertFalse(cache.get(id, "world", "silver").isPresent());
    }

    @Test
    void remove_evicts_the_matching_entry() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));

        cache.remove(id, "world", "gold");

        assertFalse(cache.get(id, "world", "gold").isPresent());
    }

    @Test
    void remove_does_not_affect_other_entries() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));
        cache.put(new Balance(id, "world", "silver"));

        cache.remove(id, "world", "gold");

        assertTrue(cache.get(id, "world", "silver").isPresent());
    }

    @Test
    void removeAll_evicts_all_entries_for_account() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));
        cache.put(new Balance(id, "nether", "gold"));
        cache.put(new Balance(id, "world", "silver"));

        cache.removeAll(id);

        assertFalse(cache.get(id, "world", "gold").isPresent());
        assertFalse(cache.get(id, "nether", "gold").isPresent());
        assertFalse(cache.get(id, "world", "silver").isPresent());
    }

    @Test
    void removeAll_does_not_affect_other_accounts() {
        var cache = cacheWithLimit(10);
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        cache.put(new Balance(id1, "world", "gold"));
        cache.put(new Balance(id2, "world", "gold"));

        cache.removeAll(id1);

        assertTrue(cache.get(id2, "world", "gold").isPresent());
    }

    @Test
    void evicts_eldest_entry_when_limit_is_exceeded() {
        var cache = cacheWithLimit(3);
        var id1 = UUID.randomUUID();
        cache.put(new Balance(id1, "world", "gold"));
        cache.put(new Balance(UUID.randomUUID(), "world", "gold"));
        cache.put(new Balance(UUID.randomUUID(), "world", "gold"));
        cache.put(new Balance(UUID.randomUUID(), "world", "gold")); // evicts id1

        assertFalse(cache.get(id1, "world", "gold").isPresent());
    }

    @Test
    void listener_is_notified_when_entry_is_evicted() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<Balance>();
        cache.setEvictionListener(evicted::add);

        var oldest = balanceIn(cache);
        balanceIn(cache);
        balanceIn(cache);
        balanceIn(cache); // triggers eviction of oldest

        assertEquals(1, evicted.size());
        assertEquals(oldest, evicted.get(0));
    }

    private static Balance balanceIn(LruBalanceCache cache) {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");
        cache.put(balance);
        return balance;
    }
}
