package com.jellyrekt.jconomy.transfer;

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
     * Returns a summary of the actions that would be taken by {@link #execute},
     * without modifying any persisted data.
     */
    TransferPreview preview();

    /**
     * Executes the import operation using the given conflict policy.
     * <p>
     * This method is always invoked on an async thread. Implementations must not
     * call the Bukkit API directly, as most Bukkit API methods are not thread-safe.
     * </p>
     *
     * @param policy the policy to apply when an account already exists in the target
     */
    void execute(ConflictPolicy policy);
}
