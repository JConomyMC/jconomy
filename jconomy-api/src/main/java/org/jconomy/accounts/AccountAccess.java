package org.jconomy.accounts;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AccountAccess {
    List<Account> getAll();
    
    Optional<Account> getByIdAndWorld(UUID accountId, String world);
    
    void save(Account account);

    void deleteBalance(UUID accountId, String world, String currency);

    /**
     * @return true if a new account was created, false if an account with the same
     *         id already exists
     */
    boolean createAccount(UUID accountId, String name);

    void deleteAccount(UUID accountId);

    Map<UUID, String> getAllAccountNames();

    Optional<String> getAccountName(UUID accountId);

    boolean renameAccount(UUID accountId, String name);

}
