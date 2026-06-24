package org.jconomy.balances;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface BalanceCache {

    Optional<Balance> get(UUID accountId, String worldName, String currency);

    void put(Balance balance);

    void remove(UUID accountId, String worldName, String currency);

    void removeAll(UUID accountId);

    default void setEvictionListener(Consumer<Balance> listener) {}
}
