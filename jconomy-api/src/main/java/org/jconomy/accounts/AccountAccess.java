package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountAccess {
    List<Account> getAll();
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);
    
    void save(Account account);

    void deleteBalance(UUID accountId, String world, String currency);

}
