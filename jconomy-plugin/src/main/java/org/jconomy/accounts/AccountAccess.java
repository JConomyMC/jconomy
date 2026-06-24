package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountAccess {

    Optional<Account> getAccount(UUID accountId);

    List<Account> getAllAccounts();

    void save(Account account);

    void deleteAccount(UUID accountId);
}
