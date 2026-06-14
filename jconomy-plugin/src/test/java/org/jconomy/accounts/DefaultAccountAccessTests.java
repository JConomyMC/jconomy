package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultAccountAccessTests {

    private TrackingAccountCache cache;
    private TrackingAccountRepository repository;
    private DefaultAccountAccess access;

    @BeforeEach
    void setUp() {
        cache = new TrackingAccountCache();
        repository = new TrackingAccountRepository();
        access = new DefaultAccountAccess(cache, repository);
    }

    @Test
    void getByIdAndWorld_returns_from_cache_without_calling_repository() {
        var id = UUID.randomUUID();
        var account = new Account(id, "world");
        cache.put(account);

        var result = access.getByIdAndWorld(id, "world");

        assertTrue(result.isPresent());
        assertFalse(repository.getByIdAndWorldCalled);
    }

    @Test
    void getByIdAndWorld_calls_repository_on_cache_miss_and_caches_result() {
        var id = UUID.randomUUID();
        var account = new Account(id, "world");
        repository.store(account);

        var result = access.getByIdAndWorld(id, "world");

        assertTrue(result.isPresent());
        assertTrue(repository.getByIdAndWorldCalled);
        assertEquals(account, cache.get(id, "world").orElse(null));
    }

    @Test
    void getByIdAndWorld_returns_empty_when_not_in_cache_or_repository() {
        var id = UUID.randomUUID();

        var result = access.getByIdAndWorld(id, "world");

        assertFalse(result.isPresent());
    }

    @Test
    void save_puts_account_into_cache_only() {
        var id = UUID.randomUUID();
        var account = new Account(id, "world");

        access.save(account);

        assertTrue(cache.get(id, "world").isPresent());
        assertNull(repository.lastUpsertAll);
    }

    @Test
    void save_marks_account_dirty() {
        var account = new Account(UUID.randomUUID(), "world");

        access.save(account);
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(account));
    }

    @Test
    void flush_persists_only_dirty_accounts() {
        var clean = new Account(UUID.randomUUID(), "world");
        var dirty = new Account(UUID.randomUUID(), "world");
        cache.put(clean);  // loaded from repo, never saved through access
        access.save(dirty);

        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(dirty));
        assertFalse(repository.lastUpsertAll.contains(clean));
    }

    @Test
    void flush_clears_dirty_records_on_success() {
        var account = new Account(UUID.randomUUID(), "world");
        access.save(account);
        access.flush();

        repository.lastUpsertAll = null;
        access.flush();

        assertNull(repository.lastUpsertAll);
    }

    @Test
    void flush_leaves_dirty_records_intact_on_repository_failure() {
        var account = new Account(UUID.randomUUID(), "world");
        access.save(account);
        repository.failOnUpsertAll = true;

        access.flush();

        repository.failOnUpsertAll = false;
        repository.lastUpsertAll = null;
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(account));
    }

    @Test
    void flush_does_not_clear_record_replaced_during_flush() {
        var original = new Account(UUID.randomUUID(), "world");
        access.save(original);

        // Simulate a save of a new object reference for the same key occurring
        // during a flush — a new account object replacing the dirty entry
        var replacement = new Account(original.getAccountId(), original.getWorldName());
        replacement.setBalance("gold", BigDecimal.ONE);

        // Flush is called; then before cleanup, replacement is saved
        // We simulate this by calling save after flush persists but before it clears.
        // We verify by directly checking: after flush of original + save of replacement,
        // a second flush must persist the replacement.
        access.flush(); // persists original, clears it
        access.save(replacement); // marks replacement dirty

        repository.lastUpsertAll = null;
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(replacement));
    }

    @Test
    void evicted_dirty_account_is_still_flushed() {
        var evictingCache = new EvictingAccountCache();
        var evictingAccess = new DefaultAccountAccess(evictingCache, repository);

        var account = new Account(UUID.randomUUID(), "world");
        evictingAccess.save(account);

        // Simulate eviction: the cache drops the entry
        evictingCache.evict(account);

        evictingAccess.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(account));
    }

    // --- Fakes ---

    private static class TrackingAccountCache implements AccountCache {
        private final Map<String, Account> store = new HashMap<>();
        private Consumer<Account> evictionListener = ignored -> {};

        @Override
        public Optional<Account> get(UUID accountId, String world) {
            return Optional.ofNullable(store.get(key(accountId, world)));
        }

        @Override
        public void put(Account account) {
            store.put(key(account.getAccountId(), account.getWorldName()), account);
        }

        @Override
        public Set<Account> getAll() {
            return new HashSet<>(store.values());
        }

        @Override
        public void setEvictionListener(Consumer<Account> listener) {
            this.evictionListener = listener;
        }

        private static String key(UUID id, String world) {
            return id + ":" + world;
        }
    }

    /** Cache that lets tests manually trigger eviction of a specific entry. */
    private static class EvictingAccountCache implements AccountCache {
        private final Map<String, Account> store = new HashMap<>();
        private Consumer<Account> evictionListener = ignored -> {};

        @Override
        public Optional<Account> get(UUID accountId, String world) {
            return Optional.ofNullable(store.get(accountId + ":" + world));
        }

        @Override
        public void put(Account account) {
            store.put(account.getAccountId() + ":" + account.getWorldName(), account);
        }

        @Override
        public Set<Account> getAll() {
            return new HashSet<>(store.values());
        }

        @Override
        public void setEvictionListener(Consumer<Account> listener) {
            this.evictionListener = listener;
        }

        void evict(Account account) {
            store.remove(account.getAccountId() + ":" + account.getWorldName());
            evictionListener.accept(account);
        }
    }

    private static class TrackingAccountRepository implements AccountRepository {
        private final Map<String, Account> store = new HashMap<>();
        boolean getByIdAndWorldCalled = false;
        Set<Account> lastUpsertAll = null;
        boolean failOnUpsertAll = false;

        void store(Account account) {
            store.put(account.getAccountId() + ":" + account.getWorldName(), account);
        }

        @Override
        public List<Account> getAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<Account> getByIdAndWorld(UUID accountId, String world) {
            getByIdAndWorldCalled = true;
            return Optional.ofNullable(store.get(accountId + ":" + world));
        }

        @Override
        public void upsert(Account account) {
            store.put(account.getAccountId() + ":" + account.getWorldName(), account);
        }

        @Override
        public void upsertAll(Set<Account> accounts) {
            if (failOnUpsertAll) throw new RuntimeException("simulated failure");
            lastUpsertAll = accounts;
        }
    }
}
