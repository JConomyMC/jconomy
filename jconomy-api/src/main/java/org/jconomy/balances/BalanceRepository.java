package org.jconomy.balances;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {

    Optional<BigDecimal> get(UUID accountId, String worldName, String currency);

    /**
     * Stores each assignment's amount, replacing any existing amount and creating
     * the balance if it does not exist.
     * <p>
     * The batch is atomic: either every assignment is persisted or none is.
     * This method returns only after the entire batch has been committed. On
     * failure it throws and leaves storage unchanged, so the caller may retry the
     * whole batch.
     * </p>
     *
     * @param assignments the absolute amounts to store
     */
    void assignAll(Collection<BalanceAssignment> assignments);

    /**
     * Adds each adjustment's delta to the stored amount, creating the balance
     * with the delta as its amount if it does not exist.
     * <p>
     * Each delta must be applied atomically against the currently stored value,
     * so that adjustments from other writers sharing the same storage are not
     * lost.
     * </p>
     * <p>
     * The batch is atomic: either every adjustment is applied or none is. This
     * method returns only after the entire batch has been committed. On failure it
     * throws and leaves storage unchanged, so the caller may retry the whole batch
     * without applying any delta twice.
     * </p>
     *
     * @param adjustments the relative changes to apply
     */
    void adjustAll(Collection<BalanceAdjustment> adjustments);

    void delete(UUID accountId, String worldName, String currency);

    void deleteByAccount(UUID accountId);
}
