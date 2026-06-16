package org.jconomy.accounts;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Account {
    
    private UUID accountId;
    private String worldName;
    private Map<String, BigDecimal> balances;

    public Account(UUID accountId, String worldName) {
        this.accountId = accountId;
        this.worldName = worldName;
        this.balances = new HashMap<>();
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getWorldName() {
        return worldName;
    }

    public BigDecimal getBalance(String currency) {
        return balances.getOrDefault(currency, BigDecimal.ZERO);
    }

    public void setBalance(String currency, BigDecimal balance) {
        balances.put(currency, balance);
    }

    public void removeBalance(String currency) {
        balances.remove(currency);
    }

    public Set<Map.Entry<String, BigDecimal>> getBalanceEntries() {
        return balances.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account other)) return false;
        if (!Objects.equals(accountId, other.accountId)) return false;
        if (!Objects.equals(worldName, other.worldName)) return false;
        if (balances.size() != other.balances.size()) return false;
        for (var entry : balances.entrySet()) {
            var otherValue = other.balances.get(entry.getKey());
            if (otherValue == null) return false;
            if (entry.getValue().compareTo(otherValue) != 0) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int balanceHash = 0;

        for (var entry : balances.entrySet()) {
            balanceHash += Objects.hashCode(entry.getKey())
                    ^ entry.getValue().stripTrailingZeros().hashCode();
        }
        return Objects.hash(accountId, worldName) ^ balanceHash;
    }
}
