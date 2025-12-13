package com.jellyrekt.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    List<Account> getAll();
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);

    Optional<String> getNameByAccountId(UUID accountId);
    
    void save(Account account);
    
}
