package org.jconomy.accounts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jconomy.storage.Flushable;

public class DefaultAccountAccess implements AccountAccess, Flushable {
    private record AccountKey(UUID accountId, String world) {}

    private final AccountCache cache;
    private final AccountRepository repository;
    private final ConcurrentHashMap<AccountKey, Account> dirtyRecords = new ConcurrentHashMap<>();

    public DefaultAccountAccess(AccountCache cache, AccountRepository repository) {
        this.cache = cache;
        this.repository = repository;
        cache.setEvictionListener(evicted -> {
            // dirtyRecords is independent of the cache; evicted entries remain
            // dirty and will be persisted on the next flush.
        });
    }

    @Override
    public List<Account> getAll() {
        return repository.getAll();
    }

    @Override
    public Optional<Account> getByIdAndWorld(UUID accountId, String world) {
        return cache.get(accountId, world).or(() -> {
            var account = repository.getByIdAndWorld(accountId, world);
            if (account.isPresent()) {
                cache.put(account.get());
            }
            return account;
        });
    }

    @Override
    public void save(Account account) {
        cache.put(account);
        dirtyRecords.put(new AccountKey(account.getAccountId(), account.getWorldName()), account);
    }

    @Override
    public void flush() {
        if (dirtyRecords.isEmpty()) return;
        var snapshot = new HashMap<>(dirtyRecords);
        try {
            repository.upsertAll(new HashSet<>(snapshot.values()));
            snapshot.forEach((k, v) -> dirtyRecords.remove(k, v));
        } catch (Exception ignored) {
            // leave dirtyRecords intact so the next flush can retry
        }
    }
}
