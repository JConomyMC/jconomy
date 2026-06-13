package com.jellyrekt.jconomy.transfer;

public interface TransferImporter {
    String getName();
    TransferPreview preview();
    void execute(ConflictPolicy policy);
}
