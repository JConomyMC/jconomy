package com.jellyrekt.jconomy.transfer;

import java.util.Set;

public record TransferPreview(
        int totalAccounts,
        int newAccounts,
        int existingAccounts,
        int conflicts,
        Set<String> currenciesAffected) {
}
