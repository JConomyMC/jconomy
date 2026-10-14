package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final ConcurrentHashMap<BalanceKey, PendingBalance> pendingBalances = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();

    public DefaultBalanceAccess(BalanceCache cache, BalanceRepository repository) {
        this.cache = cache;
        this.repository = repository;
    }

    @Override
    public Optional<BigDecimal> get(UUID accountId, String worldName, String currency) {
        var key = new BalanceKey(accountId, worldName, currency);
        var pending = pendingBalances.get(key);
        if (pending != null) return Optional.of(pending.amount());
        return cache.get(accountId, worldName, currency).or(() -> {
            var amount = repository.get(accountId, worldName, currency);
            amount.ifPresent(a -> cache.put(accountId, worldName, currency, a));
            return amount;
        });
    }

    @Override
    public BigDecimal set(UUID accountId, String worldName, String currency, BigDecimal amount) {
        pendingBalances.put(new BalanceKey(accountId, worldName, currency), PendingBalance.assignment(amount));
        return amount;
    }

    @Override
    public BigDecimal adjust(UUID accountId, String worldName, String currency, BigDecimal delta) {
        var key = new BalanceKey(accountId, worldName, currency);
        var loaded = get(accountId, worldName, currency).orElse(BigDecimal.ZERO);
        return pendingBalances.compute(key, (k, pending) -> {
            var base = pending != null ? pending : PendingBalance.unchanged(cachedAmount(k).orElse(loaded));
            return base.adjustedBy(delta);
        }).amount();
    }

    private Optional<BigDecimal> cachedAmount(BalanceKey key) {
        return cache.get(key.accountId(), key.worldName(), key.currency());
    }

    @Override
    public void flush() {
        try {
            flushPendingBalances();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to flush pending balances", e);
        }
    }

    private void flushPendingBalances() {
        synchronized (writeLock) {
            if (pendingBalances.isEmpty()) return;
            var snapshot = new HashMap<>(pendingBalances);
            var assignments = toAssignments(snapshot);
            var adjustments = toAdjustments(snapshot);
            if (!assignments.isEmpty()) repository.assignAll(assignments);
            if (!adjustments.isEmpty()) repository.adjustAll(adjustments);
            snapshot.forEach(this::reconcile);
        }
    }

    private void reconcile(BalanceKey key, PendingBalance flushed) {
        pendingBalances.computeIfPresent(key, (k, current) -> {
            if (current.equals(flushed)) {
                cache.put(k.accountId(), k.worldName(), k.currency(), current.amount());
                return null;
            }
            if (current.assigned()) return current;
            return new PendingBalance(current.amount(), current.delta().subtract(flushed.delta()), false);
        });
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
        synchronized (writeLock) {
            repository.delete(accountId, worldName, currency);
            cache.remove(accountId, worldName, currency);
            pendingBalances.remove(key);
        }
    }

    @Override
    public void deleteByAccount(UUID accountId, Runnable durableDeletion) {
        synchronized (writeLock) {
            durableDeletion.run();
            cache.removeAll(accountId);
            pendingBalances.keySet().removeIf(k -> k.accountId().equals(accountId));
        }
    }
}
