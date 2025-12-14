package com.jellyrekt.jconomy.accounts;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.jellyrekt.jconomy.config.CacheConfig;

public class LruAccountCache implements AccountCache {
    private record AccountKey(UUID accountId, String world) { }

    private final Map<AccountKey, Account> accounts;

    public LruAccountCache(CacheConfig config) {
        accounts = Collections.synchronizedMap(
            new LinkedHashMap<AccountKey, Account>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<AccountKey, Account> eldest) {
                    return size() > config.getLruLimit();
                }
            }
        );
    }

    @Override
    public Optional<Account> get(UUID accountId, String world) {
        var account = accounts.get(new AccountKey(accountId, world));
        return Optional.ofNullable(account);
    }

    @Override
    public void put(Account account) {
        accounts.put(new AccountKey(account.getAccountId(), account.getWorldName()), account);
    }

    @Override
    public Set<Account> getAll() {
        return new HashSet<>(accounts.values());
    }
}
