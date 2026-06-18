package org.jconomy.balances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jconomy.storage.Flushable;

public class DefaultBalanceAccess implements BalanceAccess, Flushable {
    private static final Logger logger = Logger.getLogger(DefaultBalanceAccess.class.getName());

    private record BalanceKey(UUID accountId, String worldName, String currency) {}

    private final BalanceCache cache;
    private final BalanceRepository repository;
    private final ConcurrentHashMap<BalanceKey, Balance> dirtyRecords = new ConcurrentHashMap<>();

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
    public void flush() {
        if (dirtyRecords.isEmpty()) return;
        var snapshot = new HashMap<>(dirtyRecords);
        try {
            repository.upsertAll(new HashSet<>(snapshot.values()));
            snapshot.forEach((k, v) -> dirtyRecords.remove(k, v));
        } catch (Exception e) {
            logger.warning("Failed to flush dirty balances: " + ExceptionUtils.getStackTrace(e));
        }
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
