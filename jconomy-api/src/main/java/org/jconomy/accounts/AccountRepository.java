package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> getAccount(UUID accountId);

    List<Account> getAllAccounts();

    void upsert(Account account);

    void deleteAccount(UUID accountId);
}
