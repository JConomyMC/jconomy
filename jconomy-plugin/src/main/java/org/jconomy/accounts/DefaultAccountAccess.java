package org.jconomy.accounts;

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

public class DefaultAccountAccess implements AccountAccess, Flushable {
    private static final Logger logger = Logger.getLogger(DefaultAccountAccess.class.getName());

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
        var key = new AccountKey(account.getAccountId(), account.getWorldName());
        cache.put(account);
        if (!account.equals(dirtyRecords.get(key))) {
            dirtyRecords.put(key, account);
        }
    }

    @Override
    public void flush() {
        if (dirtyRecords.isEmpty()) return;
        var snapshot = new HashMap<>(dirtyRecords);
        try {
            repository.upsertAll(new HashSet<>(snapshot.values()));
            snapshot.forEach((k, v) -> dirtyRecords.remove(k, v));
        } catch (Exception e) {
            logger.warning("Failed to flush dirty accounts: " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void deleteBalance(UUID accountId, String world, String currency) {
        var key = new AccountKey(accountId, world);
        repository.deleteBalance(accountId, world, currency);
        var account = dirtyRecords.get(key);
        if (account == null) {
            account = cache.get(accountId, world).orElse(null);
        }
        if (account == null) return;
        account.removeBalance(currency);
        if (account.getBalanceEntries().isEmpty()) {
            cache.remove(accountId, world);
            dirtyRecords.remove(key);
        }
    }

    @Override
    public boolean createAccount(UUID accountId, String name) {
        return repository.createAccount(accountId, name);
    }

    @Override
    public void deleteAccount(UUID accountId) {
        repository.deleteAccount(accountId);
        cache.removeAll(accountId);
        dirtyRecords.keySet().removeIf(k -> k.accountId().equals(accountId));
    }

    @Override
    public Map<UUID, String> getAllAccountNames() {
        return repository.getAllAccountNames();
    }

    @Override
    public Optional<String> getAccountName(UUID accountId) {
        return repository.getAccountName(accountId);
    }

    @Override
    public boolean renameAccount(UUID accountId, String name) {
        return repository.renameAccount(accountId, name);
    }
}
