package org.jconomy.accounts;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public interface AccountCache {

    Optional<Account> get(UUID accountId, String world);

    void put(Account account);

    Set<Account> getAll();

    default void setEvictionListener(Consumer<Account> listener) {}
}