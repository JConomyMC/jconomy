package org.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;

public interface BalanceAccess {

    Optional<Balance> get(UUID accountId, String worldName, String currency);

    void save(Balance balance);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
