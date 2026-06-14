package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jconomy.config.CacheConfig;

class LruAccountCacheTests {

    private static LruAccountCache cacheWithLimit(int limit) {
        var config = mock(CacheConfig.class);
        when(config.getLruLimit()).thenReturn(limit);
        return new LruAccountCache(config);
    }

    @Test
    void get_returns_empty_for_unknown_account() {
        var cache = cacheWithLimit(10);

        assertFalse(cache.get(UUID.randomUUID(), "world").isPresent());
    }

    @Test
    void get_returns_account_after_put() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        var account = new Account(id, "world");

        cache.put(account);

        assertEquals(account, cache.get(id, "world").orElse(null));
    }

    @Test
    void evicts_eldest_entry_when_limit_is_exceeded() {
        var cache = cacheWithLimit(3);
        var oldest = accountIn(cache, "world1");
        accountIn(cache, "world2");
        accountIn(cache, "world3");
        var newest = accountIn(cache, "world4");

        assertFalse(cache.get(oldest.getAccountId(), oldest.getWorldName()).isPresent());
        assertTrue(cache.get(newest.getAccountId(), newest.getWorldName()).isPresent());
    }

    @Test
    void getAll_returns_all_non_evicted_entries() {
        var cache = cacheWithLimit(3);
        accountIn(cache, "world1");
        var a2 = accountIn(cache, "world2");
        var a3 = accountIn(cache, "world3");
        var a4 = accountIn(cache, "world4");

        var all = cache.getAll();

        assertEquals(3, all.size());
        assertTrue(all.contains(a2));
        assertTrue(all.contains(a3));
        assertTrue(all.contains(a4));
    }

    @Test
    void listener_is_notified_when_entry_is_evicted() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<Account>();
        cache.setEvictionListener(evicted::add);

        var oldest = accountIn(cache, "world1");
        accountIn(cache, "world2");
        accountIn(cache, "world3");
        accountIn(cache, "world4"); // triggers eviction of oldest

        assertEquals(1, evicted.size());
        assertEquals(oldest, evicted.get(0));
    }

    @Test
    void listener_is_not_notified_when_no_eviction_occurs() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<Account>();
        cache.setEvictionListener(evicted::add);

        accountIn(cache, "world1");
        accountIn(cache, "world2");

        assertTrue(evicted.isEmpty());
    }

    private static Account accountIn(LruAccountCache cache, String world) {
        var account = new Account(UUID.randomUUID(), world);
        account.setBalance("gold", BigDecimal.ZERO);
        cache.put(account);
        return account;
    }
}
