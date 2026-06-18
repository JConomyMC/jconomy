package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        assertFalse(cache.get(UUID.randomUUID()).isPresent());
    }

    @Test
    void get_returns_account_after_put() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        var account = new Account(id, "Alice");

        cache.put(account);

        assertEquals(account, cache.get(id).orElse(null));
    }

    @Test
    void remove_evicts_the_entry() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        cache.put(new Account(id, "Alice"));

        cache.remove(id);

        assertFalse(cache.get(id).isPresent());
    }

    @Test
    void evicts_eldest_entry_when_limit_is_exceeded() {
        var cache = cacheWithLimit(3);
        var oldest = accountIn(cache);
        accountIn(cache);
        accountIn(cache);
        accountIn(cache); // evicts oldest

        assertFalse(cache.get(oldest.getAccountId()).isPresent());
    }

    @Test
    void listener_is_notified_when_entry_is_evicted() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<Account>();
        cache.setEvictionListener(evicted::add);

        var oldest = accountIn(cache);
        accountIn(cache);
        accountIn(cache);
        accountIn(cache); // triggers eviction of oldest

        assertEquals(1, evicted.size());
        assertEquals(oldest, evicted.get(0));
    }

    @Test
    void listener_is_not_notified_when_no_eviction_occurs() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<Account>();
        cache.setEvictionListener(evicted::add);

        accountIn(cache);
        accountIn(cache);

        assertTrue(evicted.isEmpty());
    }

    private static Account accountIn(LruAccountCache cache) {
        var account = new Account(UUID.randomUUID(), "Player");
        cache.put(account);
        return account;
    }
}

