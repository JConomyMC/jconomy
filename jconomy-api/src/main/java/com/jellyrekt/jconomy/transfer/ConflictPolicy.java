package com.jellyrekt.jconomy.transfer;

public enum ConflictPolicy {
    /** Preserve the existing account in the target when a conflict is detected. */
    SKIP,
    /** Replace the existing account in the target with the incoming data when a conflict is detected. */
    OVERWRITE
}
