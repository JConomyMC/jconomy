package org.jconomy.accounts;

import java.util.UUID;

import org.jconomy.balances.BalanceAccess;

public class AccountDeleter {
    private final AccountAccess accountAccess;
    private final BalanceAccess balanceAccess;

    public AccountDeleter(AccountAccess accountAccess, BalanceAccess balanceAccess) {
        this.accountAccess = accountAccess;
        this.balanceAccess = balanceAccess;
    }

    public void delete(UUID accountId) {
        balanceAccess.deleteByAccount(accountId, () -> accountAccess.deleteAccount(accountId));
    }
}
