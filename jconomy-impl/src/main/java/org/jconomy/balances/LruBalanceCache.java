package org.jconomy.balances;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jconomy.config.CacheConfig;

public class LruBalanceCache implements BalanceCache {

    private record BalanceKey(UUID accountId, String worldName, String currency) {}

    private final Map<BalanceKey, Balance> balances;
    private Consumer<Balance> evictionListener = ignored -> {};

    public LruBalanceCache(CacheConfig config) {
        balances = Collections.synchronizedMap(
            new LinkedHashMap<BalanceKey, Balance>(config.getLruLimit(), 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<BalanceKey, Balance> eldest) {
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
    public void setEvictionListener(Consumer<Balance> listener) {
        this.evictionListener = listener;
    }

    @Override
    public Optional<Balance> get(UUID accountId, String worldName, String currency) {
        return Optional.ofNullable(balances.get(new BalanceKey(accountId, worldName, currency)));
    }

    @Override
    public void put(Balance balance) {
        balances.put(new BalanceKey(balance.getAccountId(), balance.getWorldName(), balance.getCurrency()), balance);
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
