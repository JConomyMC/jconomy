package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface BalanceRepository {

    Optional<BigDecimal> get(UUID accountId, String worldName, String currency);

    void upsert(Balance balance);

    void upsertAll(Set<Balance> balances);

    void assignAll(Collection<BalanceAssignment> assignments);

    void adjustAll(Collection<BalanceAdjustment> adjustments);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
