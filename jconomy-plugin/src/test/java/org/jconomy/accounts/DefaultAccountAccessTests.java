package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // --- getAccount ---

    @Test
    void getAccount_returns_from_cache_without_calling_repository() {
        var id = UUID.randomUUID();
        var account = new Account(id, "Alice");
        cache.put(account);

        var result = access.getAccount(id);

        assertTrue(result.isPresent());
        assertFalse(repository.getAccountCalled);
    }

    @Test
    void getAccount_calls_repository_on_cache_miss_and_caches_result() {
        var id = UUID.randomUUID();
        var account = new Account(id, "Alice");
        repository.store(account);

        var result = access.getAccount(id);

        assertTrue(result.isPresent());
        assertTrue(repository.getAccountCalled);
        assertTrue(cache.get(id).isPresent());
    }

    @Test
    void getAccount_returns_empty_when_not_in_cache_or_repository() {
        assertFalse(access.getAccount(UUID.randomUUID()).isPresent());
    }

    // --- save ---

    @Test
    void save_persists_to_repository_and_updates_cache() {
        var id = UUID.randomUUID();
        var account = new Account(id, "Alice");

        access.save(account);

        assertTrue(repository.upsertCalled);
        assertTrue(cache.get(id).isPresent());
    }

    @Test
    void save_updates_cache_with_new_name() {
        var id = UUID.randomUUID();
        var original = new Account(id, "Alice");
        cache.put(original);

        var renamed = new Account(id, "Bob");
        access.save(renamed);

        assertEquals("Bob", cache.get(id).get().getName());
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_delegates_to_repository_and_evicts_cache() {
        var id = UUID.randomUUID();
        cache.put(new Account(id, "Alice"));

        access.deleteAccount(id);

        assertTrue(repository.deleteAccountCalled);
        assertFalse(cache.get(id).isPresent());
    }

    // --- fakes ---

    private static class TrackingAccountCache implements AccountCache {
        private final Map<UUID, Account> store = new HashMap<>();

        @Override
        public Optional<Account> get(UUID accountId) {
            return Optional.ofNullable(store.get(accountId));
        }

        @Override
        public void put(Account account) {
            store.put(account.getAccountId(), account);
        }

        @Override
        public void remove(UUID accountId) {
            store.remove(accountId);
        }
    }

    private static class TrackingAccountRepository implements AccountRepository {
        private final Map<UUID, Account> store = new HashMap<>();
        boolean getAccountCalled = false;
        boolean upsertCalled = false;
        boolean deleteAccountCalled = false;

        void store(Account account) {
            store.put(account.getAccountId(), account);
        }

        @Override
        public Optional<Account> getAccount(UUID accountId) {
            getAccountCalled = true;
            return Optional.ofNullable(store.get(accountId));
        }

        @Override
        public List<Account> getAllAccounts() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void upsert(Account account) {
            upsertCalled = true;
            store.put(account.getAccountId(), account);
        }

        @Override
        public void deleteAccount(UUID accountId) {
            deleteAccountCalled = true;
            store.remove(accountId);
        }

    }
}
