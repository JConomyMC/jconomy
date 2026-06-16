package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountRepository {
    List<Account> getAll();
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);
    
    void upsert(Account account);

    void upsertAll(Set<Account> accounts);

    void deleteBalance(UUID accountId, String world, String currency);

    /**
     * @return true if a new account was created, false if an account with the same
     *         id and world already exists
     */
    boolean createAccount(UUID accountId, String world);

    void deleteAccount(UUID accountId, String world);
}
