package com.jellyrekt.jconomy.storage;

import java.util.List;
import java.util.logging.Logger;

public class DataImportOrchestrator {
    private final List<DataImporter> importers;
    private final ImportRunRecord record;
    private final Logger logger;

    public DataImportOrchestrator(List<DataImporter> importers, ImportRunRecord record, Logger logger) {
        this.importers = importers;
        this.record = record;
        this.logger = logger;
    }

    public void run() {
        for (var importer : importers) {
            var id = importer.getId();
            if (record.isCompleted(id)) {
                logger.info(String.format("Skipping importer '%s': already completed.", id));
                continue;
            }
            try {
                importer.importData();
                record.markCompleted(id);
                logger.info(String.format("Imported data with '%s'.", id));
            } catch (Exception ex) {
                logger.warning(String.format("Data import failed for '%s': %s", id, ex.getMessage()));
            }
        }
    }
}
