package com.jellyrekt.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {
    Optional<Balance> getByPlayerIdAndCurrencyName(UUID playerId, String currencyName);
    
    void set(UUID playerId, String currencyName, double amount);
    
}
