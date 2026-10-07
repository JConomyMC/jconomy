package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface BalanceAccess {

    Optional<Balance> get(UUID accountId, String worldName, String currency);

    void save(Balance balance);

    BigDecimal adjust(UUID accountId, String worldName, String currency, BigDecimal delta);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
