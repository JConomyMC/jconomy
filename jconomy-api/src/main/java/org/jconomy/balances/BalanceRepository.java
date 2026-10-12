package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {

    Optional<BigDecimal> get(UUID accountId, String worldName, String currency);

    void assignAll(Collection<BalanceAssignment> assignments);

    void adjustAll(Collection<BalanceAdjustment> adjustments);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
