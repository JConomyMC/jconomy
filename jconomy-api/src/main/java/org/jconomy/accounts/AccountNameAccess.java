package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountNameAccess {
    List<AccountName> getAll();

    Optional<AccountName> getByAccountId(UUID accountId);

    void save(AccountName accountName);
}
