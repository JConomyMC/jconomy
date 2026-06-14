package org.jconomy.accounts;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountNameCache {
    Optional<AccountName> get(UUID accountId);

    void put(AccountName accountName);

    Set<AccountName> getAll();
}
