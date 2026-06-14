package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jconomy.storage.Flushable;

public class DefaultAccountNameAccess implements AccountNameAccess, Flushable {
    private final AccountNameCache cache;
    private final AccountNameRepository repository;

    public DefaultAccountNameAccess(AccountNameCache cache, AccountNameRepository repository) {
        this.cache = cache;
        this.repository = repository;
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
    }

    @Override
    public void flush() {
        repository.upsertAll(cache.getAll());
    }
    
}
