package com.jellyrekt.jconomy.storage;

import java.io.IOException;

import com.jellyrekt.storage.configuration.file.FileConfigurationProvider;

public class ConfigImportRunRecord implements ImportRunRecord {
    private static final String CONFIG_KEY_PREFIX = "is-importer-completed.";

    private final FileConfigurationProvider configProvider;

    public ConfigImportRunRecord(FileConfigurationProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public boolean isCompleted(String importerId) {
        return configProvider.getFileConfiguration().getBoolean(CONFIG_KEY_PREFIX + importerId, false);
    }

    @Override
    public void markCompleted(String importerId) {
        configProvider.getFileConfiguration().set(CONFIG_KEY_PREFIX + importerId, true);
        try {
            configProvider.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save import run record for '" + importerId + "'", e);
        }
    }
}
