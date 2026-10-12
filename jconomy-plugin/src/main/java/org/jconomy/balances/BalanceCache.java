package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BalanceCache {

    Optional<BigDecimal> get(UUID accountId, String worldName, String currency);

    void put(UUID accountId, String worldName, String currency, BigDecimal amount);

    void remove(UUID accountId, String worldName, String currency);

    void removeAll(UUID accountId);
}
