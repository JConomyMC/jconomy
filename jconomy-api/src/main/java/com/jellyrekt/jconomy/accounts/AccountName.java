package com.jellyrekt.jconomy.accounts;

import java.util.UUID;

public class AccountName {
    private final UUID accountId;
    private String name;

    public AccountName(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
