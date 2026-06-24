package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DefaultAccountAccess implements AccountAccess {

    private final AccountCache cache;
    private final AccountRepository repository;

    public DefaultAccountAccess(AccountCache cache, AccountRepository repository) {
        this.cache = cache;
        this.repository = repository;
    }

    @Override
    public Optional<Account> getAccount(UUID accountId) {
        return cache.get(accountId).or(() -> {
            var account = repository.getAccount(accountId);
            account.ifPresent(cache::put);
            return account;
        });
    }

    @Override
    public List<Account> getAllAccounts() {
        return repository.getAllAccounts();
    }

    @Override
    public void save(Account account) {
        repository.upsert(account);
        cache.put(account);
    }

    @Override
    public boolean createAccount(UUID accountId, String name) {
        return repository.createAccount(accountId, name);
    }

    @Override
    public void deleteAccount(UUID accountId) {
        repository.deleteAccount(accountId);
        cache.remove(accountId);
    }
}
