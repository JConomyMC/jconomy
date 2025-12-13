package com.jellyrekt.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;

public interface AccountCache {

    Optional<Double> get(UUID playerId, String currencyName);

    void put(UUID playerId, String currencyName, double amount);

}