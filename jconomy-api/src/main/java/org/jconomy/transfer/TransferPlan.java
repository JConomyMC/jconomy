package org.jconomy.transfer;

import java.util.Set;

import org.jconomy.accounts.Account;
import org.jconomy.accounts.AccountName;

/**
 * A snapshot of the accounts that will be written during a transfer operation.
 *
 * <p>
 * A {@code TransferPlan} is produced by calling
 * {@link TransferImporter#createPlan(ConflictPolicy)} or
 * {@link TransferExporter#createPlan(ConflictPolicy)}. It contains the exact set
 * of {@link Account} and {@link AccountName} records that the corresponding
 * {@code execute} call will write, already filtered according to the given
 * {@link ConflictPolicy}.
 * </p>
 *
 * <p>
 * The {@link #conflicts()} field is informational: it reports how many source
 * accounts were found to already exist in the target at preview time. Under
 * {@link ConflictPolicy#SKIP} those accounts are excluded from
 * {@link #accountsToTransfer()}; under {@link ConflictPolicy#OVERWRITE} they
 * are included.
 * </p>
 *
 * @param providerName         the {@link TransferImporter#getName()} or
 *                             {@link TransferExporter#getName()} value that
 *                             produced this plan
 * @param accountsToTransfer   the accounts that will be written to the target
 * @param accountNamesToTransfer the account name mappings that will be written
 *                             to the target
 * @param conflicts            the number of source accounts that existed in the
 *                             target at preview time
 * @param policy               the conflict policy that was applied when building
 *                             this plan
 */
public record TransferPlan(
        String providerName,
        Set<Account> accountsToTransfer,
        Set<AccountName> accountNamesToTransfer,
        int conflicts,
        ConflictPolicy policy) {
}
