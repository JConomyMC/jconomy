package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jconomy.config.CacheConfig;

public class LruBalanceCache implements BalanceCache {

    private record BalanceKey(UUID accountId, String worldName, String currency) {}

    private final Map<BalanceKey, BigDecimal> balances;

    public LruBalanceCache(CacheConfig config) {
        balances = Collections.synchronizedMap(
            new LinkedHashMap<BalanceKey, BigDecimal>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<BalanceKey, BigDecimal> eldest) {
                    return size() > config.getLruLimit();
                }
            }
        );
    }

    @Override
    public Optional<BigDecimal> get(UUID accountId, String worldName, String currency) {
        return Optional.ofNullable(balances.get(new BalanceKey(accountId, worldName, currency)));
    }

    @Override
    public void put(UUID accountId, String worldName, String currency, BigDecimal amount) {
        balances.put(new BalanceKey(accountId, worldName, currency), amount);
    }

    @Override
    public void remove(UUID accountId, String worldName, String currency) {
        balances.remove(new BalanceKey(accountId, worldName, currency));
    }

    @Override
    public void removeAll(UUID accountId) {
        balances.keySet().removeIf(k -> k.accountId().equals(accountId));
    }
}
