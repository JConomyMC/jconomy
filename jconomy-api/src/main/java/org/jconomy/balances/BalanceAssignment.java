package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceAssignment(UUID accountId, String worldName, String currency, BigDecimal amount) {
}
