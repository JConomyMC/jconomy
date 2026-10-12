package org.jconomy.balances;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
    void get_returns_amount_after_put() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();

        cache.put(id, "world", "gold", BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), cache.get(id, "world", "gold").orElseThrow());
    }

    @Test
    void get_returns_empty_for_different_world() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(id, "world", "gold", BigDecimal.ZERO);

        assertFalse(cache.get(id, "nether", "gold").isPresent());
    }

    @Test
    void get_returns_empty_for_different_currency() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(id, "world", "gold", BigDecimal.ZERO);

        assertFalse(cache.get(id, "world", "silver").isPresent());
    }

    @Test
    void remove_evicts_the_matching_entry() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(id, "world", "gold", BigDecimal.ZERO);

        cache.remove(id, "world", "gold");

        assertFalse(cache.get(id, "world", "gold").isPresent());
    }

    @Test
    void remove_does_not_affect_other_entries() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(id, "world", "gold", BigDecimal.ZERO);
        cache.put(id, "world", "silver", BigDecimal.ZERO);

        cache.remove(id, "world", "gold");

        assertTrue(cache.get(id, "world", "silver").isPresent());
    }

    @Test
    void removeAll_evicts_all_entries_for_account() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(id, "world", "gold", BigDecimal.ZERO);
        cache.put(id, "nether", "gold", BigDecimal.ZERO);
        cache.put(id, "world", "silver", BigDecimal.ZERO);

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
        cache.put(id1, "world", "gold", BigDecimal.ZERO);
        cache.put(id2, "world", "gold", BigDecimal.ZERO);

        cache.removeAll(id1);

        assertTrue(cache.get(id2, "world", "gold").isPresent());
    }

    @Test
    void evicts_eldest_entry_when_limit_is_exceeded() {
        var cache = cacheWithLimit(3);
        var id1 = UUID.randomUUID();
        cache.put(id1, "world", "gold", BigDecimal.ZERO);
        cache.put(UUID.randomUUID(), "world", "gold", BigDecimal.ZERO);
        cache.put(UUID.randomUUID(), "world", "gold", BigDecimal.ZERO);
        cache.put(UUID.randomUUID(), "world", "gold", BigDecimal.ZERO);

        assertFalse(cache.get(id1, "world", "gold").isPresent());
    }
}
