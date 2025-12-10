package com.jellyrekt.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);
    
    void save(Account account);
    
}
