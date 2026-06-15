package org.jconomy.accounts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jconomy.storage.Flushable;

public class DefaultAccountNameAccess implements AccountNameAccess, Flushable {
    private static final Logger logger = Logger.getLogger(DefaultAccountNameAccess.class.getName());

    private final AccountNameCache cache;
    private final AccountNameRepository repository;
    private final ConcurrentHashMap<UUID, AccountName> dirtyRecords = new ConcurrentHashMap<>();

    public DefaultAccountNameAccess(AccountNameCache cache, AccountNameRepository repository) {
        this.cache = cache;
        this.repository = repository;
        cache.setEvictionListener(evicted -> {
            // dirtyRecords is independent of the cache; evicted entries remain
            // dirty and will be persisted on the next flush.
        });
    }

    @Override
    public List<AccountName> getAll() {
        return repository.getAll();
    }

    @Override
    public Optional<AccountName> getByAccountId(UUID accountId) {
        return cache.get(accountId).or(() -> {
            var accountName = repository.getByAccountId(accountId);
            if (accountName.isPresent()) {
                cache.put(accountName.get());
            }
            return accountName;
        });
    }

    @Override
    public void save(AccountName accountName) {
        cache.put(accountName);
        if (!accountName.equals(dirtyRecords.get(accountName.getAccountId()))) {
            dirtyRecords.put(accountName.getAccountId(), accountName);
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
            logger.warning("Failed to flush dirty account names: " + ExceptionUtils.getStackTrace(e));
        }
    }
}
