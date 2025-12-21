package com.jellyrekt.jconomy.accounts;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AccountCache {

    Optional<Account> get(UUID accountId, String world);

    void put(Account account);

    Set<Account> getAll();
}