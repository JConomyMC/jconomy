package org.jconomy.accounts;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.jconomy.config.CacheConfig;

public class LruAccountCache implements AccountCache {
    private record AccountKey(UUID accountId, String world) { }

    private final Map<AccountKey, Account> accounts;
    private Consumer<Account> evictionListener = ignored -> {};

    public LruAccountCache(CacheConfig config) {
        accounts = Collections.synchronizedMap(
            new LinkedHashMap<AccountKey, Account>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<AccountKey, Account> eldest) {
                    if (size() > config.getLruLimit()) {
                        evictionListener.accept(eldest.getValue());
                        return true;
                    }
                    return false;
                }
            }
        );
    }

    @Override
    public void setEvictionListener(Consumer<Account> listener) {
        this.evictionListener = listener;
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
    public void remove(UUID accountId, String world) {
        accounts.remove(new AccountKey(accountId, world));
    }

    @Override
    public void removeAll(UUID accountId) {
        accounts.keySet().removeIf(k -> k.accountId().equals(accountId));
    }

    @Override
    public Set<Account> getAll() {
        return new HashSet<>(accounts.values());
    }
}
