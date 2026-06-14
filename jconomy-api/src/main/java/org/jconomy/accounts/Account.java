package org.jconomy.accounts;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
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

    public Set<Map.Entry<String, BigDecimal>> getBalanceEntries() {
        return balances.entrySet();
    }
}
