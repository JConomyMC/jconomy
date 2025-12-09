package com.jellyrekt.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {
    Optional<Account> getById(UUID accountId);
    
    void save(Account account);
    
}
