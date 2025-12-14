package com.jellyrekt.jconomy.accounts;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Account {
    
    private UUID accountId;
    private String name;
    private String worldName;
    private Map<String, BigDecimal> balances;

    public Account(UUID accountId, String worldName, String accountName) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public BigDecimal getBalance(String currency) {
        return balances.get(currency);
    }

    public void setBalance(String currency, BigDecimal balance) {
        balances.put(currency, balance);
    }

    public Set<Map.Entry<String, BigDecimal>> getBalanceEntries() {
        return balances.entrySet();
    }
}
