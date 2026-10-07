package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jconomy.storage.Flushable;

public class DefaultBalanceAccess implements BalanceAccess, Flushable {
    private static final Logger logger = Logger.getLogger(DefaultBalanceAccess.class.getName());

    private record BalanceKey(UUID accountId, String worldName, String currency) {}

    private record PendingBalance(BigDecimal amount, BigDecimal delta, boolean assigned) {

        static PendingBalance assignment(BigDecimal amount) {
            return new PendingBalance(amount, BigDecimal.ZERO, true);
        }

        static PendingBalance unchanged(BigDecimal amount) {
            return new PendingBalance(amount, BigDecimal.ZERO, false);
        }

        PendingBalance adjustedBy(BigDecimal delta) {
            return new PendingBalance(amount.add(delta), this.delta.add(delta), assigned);
        }
    }

    private final BalanceCache cache;
    private final BalanceRepository repository;
    private final ConcurrentHashMap<BalanceKey, Balance> dirtyRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<BalanceKey, PendingBalance> pendingBalances = new ConcurrentHashMap<>();

    public DefaultBalanceAccess(BalanceCache cache, BalanceRepository repository) {
        this.cache = cache;
        this.repository = repository;
        cache.setEvictionListener(evicted -> {
            // dirtyRecords is independent of the cache; evicted entries remain
            // dirty and will be persisted on the next flush.
        });
    }

    @Override
    public Optional<Balance> get(UUID accountId, String worldName, String currency) {
        return cache.get(accountId, worldName, currency).or(() -> {
            var balance = repository.get(accountId, worldName, currency);
            balance.ifPresent(cache::put);
            return balance;
        });
    }

    @Override
    public void save(Balance balance) {
        var key = new BalanceKey(balance.getAccountId(), balance.getWorldName(), balance.getCurrency());
        cache.put(balance);
        dirtyRecords.put(key, balance);
    }

    @Override
    public BigDecimal set(UUID accountId, String worldName, String currency, BigDecimal amount) {
        pendingBalances.put(new BalanceKey(accountId, worldName, currency), PendingBalance.assignment(amount));
        return amount;
    }

    @Override
    public BigDecimal adjust(UUID accountId, String worldName, String currency, BigDecimal delta) {
        var key = new BalanceKey(accountId, worldName, currency);
        var pending = pendingBalances.get(key);
        if (pending == null) {
            var current = get(accountId, worldName, currency).map(Balance::getAmount).orElse(BigDecimal.ZERO);
            pending = PendingBalance.unchanged(current);
        }
        var adjusted = pending.adjustedBy(delta);
        pendingBalances.put(key, adjusted);
        return adjusted.amount();
    }

    @Override
    public void flush() {
        try {
            flushDirtyRecords();
            flushPendingBalances();
        } catch (Exception e) {
            logger.warning("Failed to flush dirty balances: " + ExceptionUtils.getStackTrace(e));
        }
    }

    private void flushDirtyRecords() {
        if (dirtyRecords.isEmpty()) return;
        var snapshot = new HashMap<>(dirtyRecords);
        repository.upsertAll(new HashSet<>(snapshot.values()));
        snapshot.forEach((k, v) -> dirtyRecords.remove(k, v));
    }

    private void flushPendingBalances() {
        if (pendingBalances.isEmpty()) return;
        var snapshot = new HashMap<>(pendingBalances);
        var assignments = toAssignments(snapshot);
        var adjustments = toAdjustments(snapshot);
        if (!assignments.isEmpty()) repository.assignAll(assignments);
        if (!adjustments.isEmpty()) repository.adjustAll(adjustments);
        snapshot.forEach((k, v) -> pendingBalances.remove(k, v));
    }

    private static List<BalanceAssignment> toAssignments(Map<BalanceKey, PendingBalance> snapshot) {
        return snapshot.entrySet().stream()
                .filter(e -> e.getValue().assigned())
                .map(e -> new BalanceAssignment(
                        e.getKey().accountId(), e.getKey().worldName(), e.getKey().currency(), e.getValue().amount()))
                .toList();
    }

    private static List<BalanceAdjustment> toAdjustments(Map<BalanceKey, PendingBalance> snapshot) {
        return snapshot.entrySet().stream()
                .filter(e -> !e.getValue().assigned())
                .map(e -> new BalanceAdjustment(
                        e.getKey().accountId(), e.getKey().worldName(), e.getKey().currency(), e.getValue().delta()))
                .toList();
    }

    @Override
    public void delete(UUID accountId, String worldName, String currency) {
        var key = new BalanceKey(accountId, worldName, currency);
        repository.delete(accountId, worldName, currency);
        cache.remove(accountId, worldName, currency);
        dirtyRecords.remove(key);
    }

    @Override
    public void deleteByAccount(UUID accountId) {
        repository.deleteByAccount(accountId);
        cache.removeAll(accountId);
        dirtyRecords.keySet().removeIf(k -> k.accountId().equals(accountId));
    }
}
