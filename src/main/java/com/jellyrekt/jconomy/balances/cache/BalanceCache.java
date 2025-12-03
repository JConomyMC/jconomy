package com.jellyrekt.jconomy.balances.cache;

import java.util.Optional;
import java.util.UUID;

public interface BalanceCache {

    Optional<Double> get(UUID playerId, String currencyName);

    void put(UUID playerId, String currencyName, double amount);

}