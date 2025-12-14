package com.jellyrekt.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jellyrekt.jconomy.storage.Flushable;

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
        return cache.get(accountId).or(() -> repository.getByAccountId(accountId));
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
