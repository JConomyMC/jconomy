package com.jellyrekt.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountRepository {
    List<Account> getAll();
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);
    
    void upsert(Account account);

    void upsertAll(Set<Account> accounts);
}
