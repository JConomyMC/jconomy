package com.jellyrekt.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jellyrekt.jconomy.storage.Flushable;

public class DefaultAccountAccess implements AccountAccess, Flushable {
    private final AccountCache cache;
    private final AccountRepository repository;

    public DefaultAccountAccess(AccountCache cache, AccountRepository repository) {
        this.cache = cache;
        this.repository = repository;
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
    }

    @Override
    public void flush() {
        repository.upsertAll(cache.getAll());
    }
}
