package org.jconomy.accounts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultBalanceAccessTests {

    private TrackingBalanceCache cache;
    private TrackingBalanceRepository repository;
    private DefaultBalanceAccess access;

    @BeforeEach
    void setUp() {
        cache = new TrackingBalanceCache();
        repository = new TrackingBalanceRepository();
        access = new DefaultBalanceAccess(cache, repository);
    }

    // --- get ---

    @Test
    void get_returns_from_cache_without_calling_repository() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        cache.put(balance);

        var result = access.get(id, "world", "gold");

        assertTrue(result.isPresent());
        assertFalse(repository.getCalled);
    }

    @Test
    void get_calls_repository_on_cache_miss_and_caches_result() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        repository.store(balance);

        var result = access.get(id, "world", "gold");

        assertTrue(result.isPresent());
        assertTrue(repository.getCalled);
        assertTrue(cache.get(id, "world", "gold").isPresent());
    }

    @Test
    void get_returns_empty_when_not_in_cache_or_repository() {
        var result = access.get(UUID.randomUUID(), "world", "gold");

        assertFalse(result.isPresent());
    }

    // --- save ---

    @Test
    void save_puts_balance_into_cache_only() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");

        access.save(balance);

        assertTrue(cache.get(id, "world", "gold").isPresent());
        assertNull(repository.lastUpsertAll);
    }

    @Test
    void save_marks_balance_dirty() {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");

        access.save(balance);
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(balance));
    }

    @Test
    void flush_persists_only_dirty_balances() {
        var clean = new Balance(UUID.randomUUID(), "world", "gold");
        var dirty = new Balance(UUID.randomUUID(), "world", "gold");
        cache.put(clean);
        access.save(dirty);

        access.flush();

        assertTrue(repository.lastUpsertAll.contains(dirty));
        assertFalse(repository.lastUpsertAll.contains(clean));
    }

    @Test
    void flush_clears_dirty_records_on_success() {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");
        access.save(balance);
        access.flush();

        repository.lastUpsertAll = null;
        access.flush();

        assertNull(repository.lastUpsertAll);
    }

    @Test
    void flush_leaves_dirty_records_intact_on_repository_failure() {
        var balance = new Balance(UUID.randomUUID(), "world", "gold");
        access.save(balance);
        repository.failOnUpsertAll = true;

        access.flush();

        repository.failOnUpsertAll = false;
        repository.lastUpsertAll = null;
        access.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(balance));
    }

    @Test
    void flush_logs_warning_when_repository_fails() {
        var logger = Logger.getLogger(DefaultBalanceAccess.class.getName());
        var handler = new CapturingLogHandler();
        logger.addHandler(handler);
        try {
            var balance = new Balance(UUID.randomUUID(), "world", "gold");
            access.save(balance);
            repository.failOnUpsertAll = true;

            access.flush();

            assertTrue(handler.hasWarning());
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    void evicted_dirty_balance_is_still_flushed() {
        var evictingCache = new EvictingBalanceCache();
        var evictingAccess = new DefaultBalanceAccess(evictingCache, repository);

        var balance = new Balance(UUID.randomUUID(), "world", "gold");
        evictingAccess.save(balance);
        evictingCache.evict(balance);

        evictingAccess.flush();

        assertNotNull(repository.lastUpsertAll);
        assertTrue(repository.lastUpsertAll.contains(balance));
    }

    // --- delete ---

    @Test
    void delete_delegates_to_repository() {
        var id = UUID.randomUUID();

        access.delete(id, "world", "gold");

        assertTrue(repository.deleteCalled);
        assertEquals(id, repository.lastDeletedId);
        assertEquals("world", repository.lastDeletedWorld);
        assertEquals("gold", repository.lastDeletedCurrency);
    }

    @Test
    void delete_evicts_from_cache() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        access.save(balance);

        access.delete(id, "world", "gold");

        assertFalse(cache.get(id, "world", "gold").isPresent());
    }

    @Test
    void delete_removes_dirty_record() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        access.save(balance);

        access.delete(id, "world", "gold");

        repository.lastUpsertAll = null;
        access.flush();

        assertNull(repository.lastUpsertAll);
    }

    // --- deleteByAccount ---

    @Test
    void deleteByAccount_delegates_to_repository() {
        var id = UUID.randomUUID();

        access.deleteByAccount(id);

        assertTrue(repository.deleteByAccountCalled);
        assertEquals(id, repository.lastDeleteByAccountId);
    }

    @Test
    void deleteByAccount_evicts_all_account_entries_from_cache() {
        var id = UUID.randomUUID();
        cache.put(new Balance(id, "world", "gold"));
        cache.put(new Balance(id, "nether", "gold"));

        access.deleteByAccount(id);

        assertFalse(cache.get(id, "world", "gold").isPresent());
        assertFalse(cache.get(id, "nether", "gold").isPresent());
    }

    @Test
    void deleteByAccount_removes_all_dirty_records_for_account() {
        var id = UUID.randomUUID();
        var b1 = new Balance(id, "world", "gold");
        var b2 = new Balance(id, "nether", "gold");
        access.save(b1);
        access.save(b2);

        access.deleteByAccount(id);

        repository.lastUpsertAll = null;
        access.flush();

        assertNull(repository.lastUpsertAll);
    }

    // --- fakes ---

    private static class CapturingLogHandler extends Handler {
        private boolean warningSeen = false;

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) warningSeen = true;
        }

        @Override public void flush() {}
        @Override public void close() {}

        boolean hasWarning() { return warningSeen; }
    }

    private static class TrackingBalanceCache implements BalanceCache {
        private final Map<String, Balance> store = new HashMap<>();

        @Override
        public Optional<Balance> get(UUID accountId, String worldName, String currency) {
            return Optional.ofNullable(store.get(key(accountId, worldName, currency)));
        }

        @Override
        public void put(Balance balance) {
            store.put(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
        }

        @Override
        public void remove(UUID accountId, String worldName, String currency) {
            store.remove(key(accountId, worldName, currency));
        }

        @Override
        public void removeAll(UUID accountId) {
            store.keySet().removeIf(k -> k.startsWith(accountId + ":"));
        }

        private static String key(UUID id, String world, String currency) {
            return id + ":" + world + ":" + currency;
        }
    }

    private static class EvictingBalanceCache implements BalanceCache {
        private final Map<String, Balance> store = new HashMap<>();
        private Consumer<Balance> evictionListener = ignored -> {};

        @Override
        public Optional<Balance> get(UUID accountId, String worldName, String currency) {
            return Optional.ofNullable(store.get(key(accountId, worldName, currency)));
        }

        @Override
        public void put(Balance balance) {
            store.put(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
        }

        @Override
        public void remove(UUID accountId, String worldName, String currency) {
            store.remove(key(accountId, worldName, currency));
        }

        @Override
        public void removeAll(UUID accountId) {
            store.keySet().removeIf(k -> k.startsWith(accountId + ":"));
        }

        @Override
        public void setEvictionListener(Consumer<Balance> listener) {
            this.evictionListener = listener;
        }

        void evict(Balance balance) {
            store.remove(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()));
            evictionListener.accept(balance);
        }

        private static String key(UUID id, String world, String currency) {
            return id + ":" + world + ":" + currency;
        }
    }

    private static class TrackingBalanceRepository implements BalanceRepository {
        private final Map<String, Balance> store = new HashMap<>();
        boolean getCalled = false;
        Set<Balance> lastUpsertAll = null;
        boolean failOnUpsertAll = false;
        boolean deleteCalled = false;
        UUID lastDeletedId = null;
        String lastDeletedWorld = null;
        String lastDeletedCurrency = null;
        boolean deleteByAccountCalled = false;
        UUID lastDeleteByAccountId = null;

        void store(Balance balance) {
            store.put(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
        }

        @Override
        public Optional<Balance> get(UUID accountId, String worldName, String currency) {
            getCalled = true;
            return Optional.ofNullable(store.get(key(accountId, worldName, currency)));
        }

        @Override
        public void upsert(Balance balance) {
            store.put(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
        }

        @Override
        public void upsertAll(Set<Balance> balances) {
            if (failOnUpsertAll) throw new RuntimeException("simulated failure");
            lastUpsertAll = balances;
        }

        @Override
        public void delete(UUID accountId, String worldName, String currency) {
            deleteCalled = true;
            lastDeletedId = accountId;
            lastDeletedWorld = worldName;
            lastDeletedCurrency = currency;
        }

        @Override
        public void deleteByAccount(UUID accountId) {
            deleteByAccountCalled = true;
            lastDeleteByAccountId = accountId;
        }

        private static String key(UUID id, String world, String currency) {
            return id + ":" + world + ":" + currency;
        }
    }
}
