package org.jconomy.accounts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jconomy.config.CacheConfig;

public class LruAccountCache implements AccountCache {

    private final Map<UUID, Account> accounts;
    private Consumer<Account> evictionListener = ignored -> {};

    public LruAccountCache(CacheConfig config) {
        accounts = Collections.synchronizedMap(
            new LinkedHashMap<UUID, Account>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<UUID, Account> eldest) {
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
    public Optional<Account> get(UUID accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public void put(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    @Override
    public void remove(UUID accountId) {
        accounts.remove(accountId);
    }
}
