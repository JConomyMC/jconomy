package com.jellyrekt.jconomy.accounts.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.jellyrekt.jconomy.accounts.AccountCache;
import com.jellyrekt.jconomy.config.CacheConfig;

public class LruAccountCache implements AccountCache {
    private record BalanceCacheKey(UUID playerId, String currencyName) { }

    private final Map<BalanceCacheKey, Double> amounts;

    public LruAccountCache(CacheConfig config) {
        amounts = Collections.synchronizedMap(
            new LinkedHashMap<BalanceCacheKey, Double>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<BalanceCacheKey, Double> eldest) {
                    return size() > config.getLruLimit();
                }
            }
        );
    }

    @Override
    public Optional<Double> get(UUID playerId, String currencyName) {
        var amount = amounts.get(new BalanceCacheKey(playerId, currencyName));
        return Optional.ofNullable(amount);
    }

    @Override
    public void put(UUID playerId, String currencyName, double amount) {
        amounts.put(new BalanceCacheKey(playerId, currencyName), amount);
    }
}
