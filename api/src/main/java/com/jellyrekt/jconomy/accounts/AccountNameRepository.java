package com.jellyrekt.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountNameRepository {
    List<AccountName> getAll();

    Optional<AccountName> getByAccountId(UUID accountId);

    void save(AccountName accountName);
}
