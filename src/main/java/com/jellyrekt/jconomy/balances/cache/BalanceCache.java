package com.jellyrekt.jconomy.balances.cache;

import java.util.Optional;
import java.util.UUID;

import com.jellyrekt.jconomy.balances.Balance;

public interface BalanceCache {

    Optional<Balance> get(UUID playerId, String currencyName);

    void put(UUID playerId, String currencyName, double amount);

}