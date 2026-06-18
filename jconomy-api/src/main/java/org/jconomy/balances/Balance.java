package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Balance {

    private final UUID accountId;
    private final String worldName;
    private final String currency;
    private BigDecimal amount;

    public Balance(UUID accountId, String worldName, String currency) {
        this.accountId = accountId;
        this.worldName = worldName;
        this.currency = currency;
        this.amount = BigDecimal.ZERO;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balance other)) return false;
        return Objects.equals(accountId, other.accountId)
                && Objects.equals(worldName, other.worldName)
                && Objects.equals(currency, other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, worldName, currency);
    }
}
