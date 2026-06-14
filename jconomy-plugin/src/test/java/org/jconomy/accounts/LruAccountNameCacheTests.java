package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.jconomy.config.CacheConfig;

class LruAccountNameCacheTests {

    private static LruAccountNameCache cacheWithLimit(int limit) {
        var config = mock(CacheConfig.class);
        when(config.getLruLimit()).thenReturn(limit);
        return new LruAccountNameCache(config);
    }

    @Test
    void get_returns_empty_for_unknown_account() {
        var cache = cacheWithLimit(10);

        assertFalse(cache.get(UUID.randomUUID()).isPresent());
    }

    @Test
    void get_returns_account_name_after_put() {
        var cache = cacheWithLimit(10);
        var id = UUID.randomUUID();
        var accountName = new AccountName(id);
        accountName.setName("Alice");

        cache.put(accountName);

        assertEquals("Alice", cache.get(id).map(AccountName::getName).orElse(null));
    }

    @Test
    void evicts_eldest_entry_when_limit_is_exceeded() {
        var cache = cacheWithLimit(3);
        var oldest = accountNameIn(cache);
        accountNameIn(cache);
        accountNameIn(cache);
        var newest = accountNameIn(cache);

        assertFalse(cache.get(oldest.getAccountId()).isPresent());
        assertTrue(cache.get(newest.getAccountId()).isPresent());
    }

    @Test
    void getAll_returns_all_non_evicted_entries() {
        var cache = cacheWithLimit(3);
        accountNameIn(cache);
        var a2 = accountNameIn(cache);
        var a3 = accountNameIn(cache);
        var a4 = accountNameIn(cache);

        var all = cache.getAll();

        assertEquals(3, all.size());
        assertTrue(all.contains(a2));
        assertTrue(all.contains(a3));
        assertTrue(all.contains(a4));
    }

    @Test
    void listener_is_notified_when_entry_is_evicted() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<AccountName>();
        cache.setEvictionListener(evicted::add);

        var oldest = accountNameIn(cache);
        accountNameIn(cache);
        accountNameIn(cache);
        accountNameIn(cache); // triggers eviction of oldest

        assertEquals(1, evicted.size());
        assertEquals(oldest, evicted.get(0));
    }

    @Test
    void listener_is_not_notified_when_no_eviction_occurs() {
        var cache = cacheWithLimit(3);
        var evicted = new ArrayList<AccountName>();
        cache.setEvictionListener(evicted::add);

        accountNameIn(cache);
        accountNameIn(cache);

        assertTrue(evicted.isEmpty());
    }

    private static AccountName accountNameIn(LruAccountNameCache cache) {
        var accountName = new AccountName(UUID.randomUUID());
        accountName.setName("Player" + accountName.getAccountId());
        cache.put(accountName);
        return accountName;
    }
}
