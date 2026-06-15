package org.jconomy.transfer;

public interface TransferImporter {

    /**
     * Returns the unique identifier for this importer.
     * <p>
     * This value is used verbatim as the {@code <provider>} argument in
     * {@code /jconomy import} commands. It must be unique among all registered
     * importers and must not contain spaces.
     * </p>
     */
    String getName();

    /**
     * Builds a {@link TransferPlan} representing the accounts that would be
     * written by {@link #execute}, without modifying any persisted data.
     * <p>
     * The returned plan already reflects the given {@link ConflictPolicy}:
     * conflicting accounts are excluded from the plan under
     * {@link ConflictPolicy#SKIP} and included under
     * {@link ConflictPolicy#OVERWRITE}.
     * </p>
     *
     * @param policy the conflict policy to apply when building the plan
     * @return a snapshot of what this importer would write
     */
    TransferPlan createPlan(ConflictPolicy policy);

    /**
     * Executes the import operation described by the given plan.
     * <p>
     * This method is always invoked on an async thread. Implementations must not
     * call the Bukkit API directly, as most Bukkit API methods are not thread-safe.
     * </p>
     *
     * @param plan the plan produced by {@link #createPlan(ConflictPolicy)} to execute
     */
    void execute(TransferPlan plan);
}
