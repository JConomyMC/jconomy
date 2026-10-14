package org.jconomy.accounts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Optional<Account> getAccount(UUID accountId);

    List<Account> getAllAccounts();

    void upsert(Account account);

    /**
     * Deletes the account and every balance belonging to it.
     * <p>
     * The deletion is atomic: either the account and all of its balances are
     * removed or nothing is. This method returns only after the deletion has been
     * committed. On failure it throws and leaves storage unchanged. Deleting an
     * account that does not exist does nothing.
     * </p>
     *
     * @param accountId the account to delete
     */
    void deleteAccount(UUID accountId);
}
