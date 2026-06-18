package org.jconomy.balances;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BalanceRepository {

    Optional<Balance> get(UUID accountId, String worldName, String currency);

    void upsert(Balance balance);

    void upsertAll(Set<Balance> balances);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
