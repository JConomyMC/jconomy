package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceAdjustment(UUID accountId, String worldName, String currency, BigDecimal delta) {
}
