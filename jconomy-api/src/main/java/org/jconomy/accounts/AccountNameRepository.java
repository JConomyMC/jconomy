package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountNameRepository {
    List<AccountName> getAll();

    Optional<AccountName> getByAccountId(UUID accountId);

    void upsert(AccountName accountName);

    void upsertAll(Set<AccountName> accountNames);
}
