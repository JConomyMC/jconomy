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
    void flush_calls_upsertAll_with_all_cached_accounts() {
        var account1 = new Account(UUID.randomUUID(), "world");
        var account2 = new Account(UUID.randomUUID(), "world");
        cache.put(account1);
        cache.put(account2);

        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(account1));
        assertTrue(repository.lastUpsertAll.contains(account2));
    }

    // --- Fakes ---

    private static class TrackingAccountCache implements AccountCache {
        private final Map<String, Account> store = new HashMap<>();

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

        private static String key(UUID id, String world) {
            return id + ":" + world;
        }
    }

    private static class TrackingAccountRepository implements AccountRepository {
        private final Map<String, Account> store = new HashMap<>();
        boolean getByIdAndWorldCalled = false;
        Set<Account> lastUpsertAll = null;

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
            lastUpsertAll = accounts;
        }
    }
}
