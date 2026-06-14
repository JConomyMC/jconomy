package com.jellyrekt.jconomy.transfer;

import java.util.Set;

public record TransferPreview(
        int totalAccounts,
        int newAccounts,
        int existingAccounts,
        /** Accounts present in both the source and the target at the time of the operation. */
        int conflicts,
        Set<String> currenciesAffected) {
}
