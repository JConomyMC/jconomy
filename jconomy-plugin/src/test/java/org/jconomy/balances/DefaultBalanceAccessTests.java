package org.jconomy.balances;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    void get_returns_pending_adjusted_amount_over_cached_amount() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        var result = access.get(id, "world", "gold");

        assertEquals(BigDecimal.valueOf(110), result.orElseThrow());
    }

    @Test
    void get_returns_pending_assigned_amount_without_calling_repository() {
        var id = UUID.randomUUID();
        access.set(id, "world", "gold", BigDecimal.valueOf(50));

        var result = access.get(id, "world", "gold");

        assertEquals(BigDecimal.valueOf(50), result.orElseThrow());
        assertFalse(repository.getCalled);
    }

    @Test
    void get_returns_empty_when_not_in_cache_or_repository() {
        var result = access.get(UUID.randomUUID(), "world", "gold");

        assertFalse(result.isPresent());
    }

    // --- flush ---

    @Test
    void flush_sends_only_pending_balances() {
        var id = UUID.randomUUID();
        cache.put(new Balance(UUID.randomUUID(), "world", "gold"));
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        access.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))),
                List.copyOf(repository.lastAdjustAll));
    }

    @Test
    void flush_clears_pending_balances_on_success() {
        access.adjust(UUID.randomUUID(), "world", "gold", BigDecimal.valueOf(10));
        access.flush();

        repository.lastAdjustAll = null;
        access.flush();

        assertNull(repository.lastAdjustAll);
    }

    @Test
    void flush_keeps_pending_balances_on_repository_failure() {
        var id = UUID.randomUUID();
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        repository.failOnAdjustAll = true;

        access.flush();

        repository.failOnAdjustAll = false;
        access.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))),
                List.copyOf(repository.lastAdjustAll));
    }

    @Test
    void flush_logs_warning_when_repository_fails() {
        var logger = Logger.getLogger(DefaultBalanceAccess.class.getName());
        var handler = new CapturingLogHandler();
        logger.addHandler(handler);
        try {
            access.adjust(UUID.randomUUID(), "world", "gold", BigDecimal.valueOf(10));
            repository.failOnAdjustAll = true;

            access.flush();

            assertTrue(handler.hasWarning());
        } finally {
            logger.removeHandler(handler);
        }
    }

    // --- adjust ---

    @Test
    void adjust_returns_cached_amount_plus_delta() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);

        var result = access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        assertEquals(BigDecimal.valueOf(110), result);
    }

    @Test
    void flush_sends_adjusted_balance_as_single_adjustment() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        access.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))),
                List.copyOf(repository.lastAdjustAll));
    }

    @Test
    void adjust_loads_amount_from_repository_on_cache_miss() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        repository.store(balance);

        var result = access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        assertEquals(BigDecimal.valueOf(110), result);
    }

    @Test
    void repeated_adjusts_return_running_amount() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);

        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        var result = access.adjust(id, "world", "gold", BigDecimal.valueOf(20));

        assertEquals(BigDecimal.valueOf(130), result);
    }

    @Test
    void flush_sends_repeated_adjusts_as_single_combined_adjustment() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        access.adjust(id, "world", "gold", BigDecimal.valueOf(20));

        access.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(30))),
                List.copyOf(repository.lastAdjustAll));
    }

    // --- set ---

    @Test
    void set_returns_assigned_amount() {
        var id = UUID.randomUUID();

        var result = access.set(id, "world", "gold", BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), result);
    }

    @Test
    void flush_sends_set_balance_as_single_assignment() {
        var id = UUID.randomUUID();
        access.set(id, "world", "gold", BigDecimal.valueOf(50));

        access.flush();

        assertEquals(
                List.of(new BalanceAssignment(id, "world", "gold", BigDecimal.valueOf(50))),
                List.copyOf(repository.lastAssignAll));
        assertNull(repository.lastAdjustAll);
    }

    @Test
    void set_then_adjust_flushes_single_combined_assignment() {
        var id = UUID.randomUUID();
        access.set(id, "world", "gold", BigDecimal.valueOf(50));
        var result = access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        access.flush();

        assertEquals(BigDecimal.valueOf(60), result);
        assertEquals(
                List.of(new BalanceAssignment(id, "world", "gold", BigDecimal.valueOf(60))),
                List.copyOf(repository.lastAssignAll));
        assertNull(repository.lastAdjustAll);
    }

    // --- flush reconcile ---

    @Test
    void flush_caches_flushed_amount() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        access.flush();

        assertEquals(BigDecimal.valueOf(110), access.get(id, "world", "gold").orElseThrow());
        assertFalse(repository.getCalled);
    }

    @Test
    void adjust_during_flush_leaves_only_remainder_pending() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        repository.duringAdjustAll = () -> {
            repository.duringAdjustAll = () -> {};
            access.adjust(id, "world", "gold", BigDecimal.valueOf(5));
        };
        access.flush();

        access.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(5))),
                List.copyOf(repository.lastAdjustAll));
        assertEquals(BigDecimal.valueOf(115), access.get(id, "world", "gold").orElseThrow());
    }

    @Test
    void set_during_flush_stays_pending() {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        cache.put(balance);
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        repository.duringAdjustAll = () -> {
            repository.duringAdjustAll = () -> {};
            access.set(id, "world", "gold", BigDecimal.valueOf(50));
        };
        access.flush();

        access.flush();

        assertEquals(
                List.of(new BalanceAssignment(id, "world", "gold", BigDecimal.valueOf(50))),
                List.copyOf(repository.lastAssignAll));
    }

    // --- concurrency ---

    @Test
    void concurrent_adjusts_after_simultaneous_load_are_both_applied() throws Exception {
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        repository.store(balance);
        var bothLoading = new CyclicBarrier(2);
        repository.duringGet = () -> awaitQuietly(bothLoading);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> access.adjust(id, "world", "gold", BigDecimal.valueOf(10)));
            var second = executor.submit(() -> access.adjust(id, "world", "gold", BigDecimal.valueOf(20)));
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        }

        assertEquals(BigDecimal.valueOf(130), access.get(id, "world", "gold").orElseThrow());
    }

    private static void awaitQuietly(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void evicted_pending_balance_is_still_flushed() {
        var evictingCache = new EvictingBalanceCache();
        var evictingAccess = new DefaultBalanceAccess(evictingCache, repository);
        var id = UUID.randomUUID();
        var balance = new Balance(id, "world", "gold");
        balance.setAmount(BigDecimal.valueOf(100));
        evictingCache.put(balance);
        evictingAccess.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        evictingCache.evict(balance);

        evictingAccess.flush();

        assertEquals(
                List.of(new BalanceAdjustment(id, "world", "gold", BigDecimal.valueOf(10))),
                List.copyOf(repository.lastAdjustAll));
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
        cache.put(new Balance(id, "world", "gold"));

        access.delete(id, "world", "gold");

        assertFalse(cache.get(id, "world", "gold").isPresent());
    }

    @Test
    void delete_discards_pending_balance() {
        var id = UUID.randomUUID();
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));

        access.delete(id, "world", "gold");
        access.flush();

        assertNull(repository.lastAdjustAll);
        assertTrue(access.get(id, "world", "gold").isEmpty());
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
    void deleteByAccount_discards_pending_balances_for_account_only() {
        var id = UUID.randomUUID();
        var otherId = UUID.randomUUID();
        access.adjust(id, "world", "gold", BigDecimal.valueOf(10));
        access.set(id, "nether", "gold", BigDecimal.valueOf(20));
        access.adjust(otherId, "world", "gold", BigDecimal.valueOf(30));

        access.deleteByAccount(id);
        access.flush();

        assertNull(repository.lastAssignAll);
        assertEquals(
                List.of(new BalanceAdjustment(otherId, "world", "gold", BigDecimal.valueOf(30))),
                List.copyOf(repository.lastAdjustAll));
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
        private final Map<String, Balance> store = new ConcurrentHashMap<>();

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
        boolean failOnAdjustAll = false;
        Collection<BalanceAssignment> lastAssignAll = null;
        Collection<BalanceAdjustment> lastAdjustAll = null;
        Runnable duringAdjustAll = () -> {};
        Runnable duringGet = () -> {};
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
            duringGet.run();
            return Optional.ofNullable(store.get(key(accountId, worldName, currency)));
        }

        @Override
        public void upsert(Balance balance) {
            store.put(key(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
        }

        @Override
        public void upsertAll(Set<Balance> balances) {
        }

        @Override
        public void assignAll(Collection<BalanceAssignment> assignments) {
            lastAssignAll = assignments;
        }

        @Override
        public void adjustAll(Collection<BalanceAdjustment> adjustments) {
            if (failOnAdjustAll) throw new RuntimeException("simulated failure");
            lastAdjustAll = adjustments;
            duringAdjustAll.run();
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
