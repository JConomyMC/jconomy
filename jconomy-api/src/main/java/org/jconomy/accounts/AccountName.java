package org.jconomy.accounts;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountName other)) return false;
        return Objects.equals(accountId, other.accountId)
                && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, name);
    }
}
