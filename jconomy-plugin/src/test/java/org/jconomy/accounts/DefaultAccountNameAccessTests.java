package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

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

class DefaultAccountNameAccessTests {

    private TrackingAccountNameCache cache;
    private TrackingAccountNameRepository repository;
    private DefaultAccountNameAccess access;

    @BeforeEach
    void setUp() {
        cache = new TrackingAccountNameCache();
        repository = new TrackingAccountNameRepository();
        access = new DefaultAccountNameAccess(cache, repository);
    }

    @Test
    void getByAccountId_returns_from_cache_without_calling_repository() {
        var id = UUID.randomUUID();
        var accountName = accountNameWith(id, "Alice");
        cache.put(accountName);

        var result = access.getByAccountId(id);

        assertTrue(result.isPresent());
        assertFalse(repository.getByAccountIdCalled);
    }

    @Test
    void getByAccountId_calls_repository_on_cache_miss_and_caches_result() {
        var id = UUID.randomUUID();
        var accountName = accountNameWith(id, "Alice");
        repository.store(accountName);

        var result = access.getByAccountId(id);

        assertTrue(result.isPresent());
        assertTrue(repository.getByAccountIdCalled);
        assertEquals(accountName, cache.get(id).orElse(null));
    }

    @Test
    void getByAccountId_returns_empty_when_not_in_cache_or_repository() {
        var result = access.getByAccountId(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    void save_puts_account_name_into_cache_only() {
        var id = UUID.randomUUID();
        var accountName = accountNameWith(id, "Alice");

        access.save(accountName);

        assertTrue(cache.get(id).isPresent());
        assertNull(repository.lastUpsertAll);
    }

    @Test
    void save_marks_account_name_dirty() {
        var accountName = accountNameWith(UUID.randomUUID(), "Alice");

        access.save(accountName);
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(accountName));
    }

    @Test
    void flush_persists_only_dirty_account_names() {
        var clean = accountNameWith(UUID.randomUUID(), "Alice");
        var dirty = accountNameWith(UUID.randomUUID(), "Bob");
        cache.put(clean); // loaded from repo, never saved through access
        access.save(dirty);

        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(dirty));
        assertFalse(repository.lastUpsertAll.contains(clean));
    }

    @Test
    void flush_clears_dirty_records_on_success() {
        var accountName = accountNameWith(UUID.randomUUID(), "Alice");
        access.save(accountName);
        access.flush();

        repository.lastUpsertAll = null;
        access.flush();

        assertNull(repository.lastUpsertAll);
    }

    @Test
    void flush_leaves_dirty_records_intact_on_repository_failure() {
        var accountName = accountNameWith(UUID.randomUUID(), "Alice");
        access.save(accountName);
        repository.failOnUpsertAll = true;

        access.flush();

        repository.failOnUpsertAll = false;
        repository.lastUpsertAll = null;
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(accountName));
    }

    @Test
    void evicted_dirty_account_name_is_still_flushed() {
        var evictingCache = new EvictingAccountNameCache();
        var evictingAccess = new DefaultAccountNameAccess(evictingCache, repository);

        var accountName = accountNameWith(UUID.randomUUID(), "Alice");
        evictingAccess.save(accountName);

        evictingCache.evict(accountName);

        evictingAccess.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(accountName));
    }

    private static AccountName accountNameWith(UUID id, String name) {
        var accountName = new AccountName(id);
        accountName.setName(name);
        return accountName;
    }

    // --- Fakes ---

    private static class TrackingAccountNameCache implements AccountNameCache {
        private final Map<UUID, AccountName> store = new HashMap<>();
        private Consumer<AccountName> evictionListener = ignored -> {};

        @Override
        public Optional<AccountName> get(UUID accountId) {
            return Optional.ofNullable(store.get(accountId));
        }

        @Override
        public void put(AccountName accountName) {
            store.put(accountName.getAccountId(), accountName);
        }

        @Override
        public Set<AccountName> getAll() {
            return new HashSet<>(store.values());
        }

        @Override
        public void setEvictionListener(Consumer<AccountName> listener) {
            this.evictionListener = listener;
        }
    }

    /** Cache that lets tests manually trigger eviction of a specific entry. */
    private static class EvictingAccountNameCache implements AccountNameCache {
        private final Map<UUID, AccountName> store = new HashMap<>();
        private Consumer<AccountName> evictionListener = ignored -> {};

        @Override
        public Optional<AccountName> get(UUID accountId) {
            return Optional.ofNullable(store.get(accountId));
        }

        @Override
        public void put(AccountName accountName) {
            store.put(accountName.getAccountId(), accountName);
        }

        @Override
        public Set<AccountName> getAll() {
            return new HashSet<>(store.values());
        }

        @Override
        public void setEvictionListener(Consumer<AccountName> listener) {
            this.evictionListener = listener;
        }

        void evict(AccountName accountName) {
            store.remove(accountName.getAccountId());
            evictionListener.accept(accountName);
        }
    }

    private static class TrackingAccountNameRepository implements AccountNameRepository {
        private final Map<UUID, AccountName> store = new HashMap<>();
        boolean getByAccountIdCalled = false;
        Set<AccountName> lastUpsertAll = null;
        boolean failOnUpsertAll = false;

        void store(AccountName accountName) {
            store.put(accountName.getAccountId(), accountName);
        }

        @Override
        public List<AccountName> getAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public Optional<AccountName> getByAccountId(UUID accountId) {
            getByAccountIdCalled = true;
            return Optional.ofNullable(store.get(accountId));
        }

        @Override
        public void upsert(AccountName accountName) {
            store.put(accountName.getAccountId(), accountName);
        }

        @Override
        public void upsertAll(Set<AccountName> accountNames) {
            if (failOnUpsertAll) throw new RuntimeException("simulated failure");
            lastUpsertAll = accountNames;
        }
    }
}
