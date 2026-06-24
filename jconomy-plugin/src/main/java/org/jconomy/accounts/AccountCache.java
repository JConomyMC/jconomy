package org.jconomy.accounts;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface AccountCache {

    Optional<Account> get(UUID accountId);

    void put(Account account);

    void remove(UUID accountId);

    default void setEvictionListener(Consumer<Account> listener) {}
}
