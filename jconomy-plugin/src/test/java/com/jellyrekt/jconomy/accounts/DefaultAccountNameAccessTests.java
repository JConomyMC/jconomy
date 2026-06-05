package com.jellyrekt.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

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
    void flush_calls_upsertAll_with_all_cached_account_names() {
        var name1 = accountNameWith(UUID.randomUUID(), "Alice");
        var name2 = accountNameWith(UUID.randomUUID(), "Bob");
        cache.put(name1);
        cache.put(name2);

        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(name1));
        assertTrue(repository.lastUpsertAll.contains(name2));
    }

    private static AccountName accountNameWith(UUID id, String name) {
        var accountName = new AccountName(id);
        accountName.setName(name);
        return accountName;
    }

    // --- Fakes ---

    private static class TrackingAccountNameCache implements AccountNameCache {
        private final Map<UUID, AccountName> store = new HashMap<>();

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
    }

    private static class TrackingAccountNameRepository implements AccountNameRepository {
        private final Map<UUID, AccountName> store = new HashMap<>();
        boolean getByAccountIdCalled = false;
        Set<AccountName> lastUpsertAll = null;

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
            lastUpsertAll = accountNames;
        }
    }
}
