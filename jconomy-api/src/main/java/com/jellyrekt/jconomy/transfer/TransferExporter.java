package com.jellyrekt.jconomy.transfer;

public interface TransferExporter {
    String getName();
    TransferPreview preview();
    void execute(ConflictPolicy policy);
}
