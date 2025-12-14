package com.jellyrekt.jconomy.accounts;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.jellyrekt.jconomy.config.CacheConfig;

public class LruAccountNameCache implements AccountNameCache {
    private final Map<UUID, AccountName> accountNames;

    public LruAccountNameCache(CacheConfig config) {
        accountNames = Collections.synchronizedMap(
            new LinkedHashMap<>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<UUID, AccountName> eldest) {
                    return size() > config.getLruLimit();
                }
            }
        );
    }
    
    public Optional<AccountName> get(UUID accountId) {
        return Optional.of(accountNames.get(accountId));
    }

    public void put(AccountName accountName) {
        accountNames.put(accountName.getAccountId(), accountName);
    }

    public Set<AccountName> getAll() {
        return new HashSet<>(accountNames.values());
    }
}
