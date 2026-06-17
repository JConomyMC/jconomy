package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountAccess {

    Optional<Account> getAccount(UUID accountId);

    List<Account> getAllAccounts();

    void save(Account account);

    /**
     * @return true if a new account was created, false if an account with the same
     *         id already exists
     */
    boolean createAccount(UUID accountId, String name);

    void deleteAccount(UUID accountId);
}
