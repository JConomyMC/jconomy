package com.jellyrekt.jconomy.storage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jellyrekt.storage.configuration.file.FileConfigurationProvider;
import com.jellyrekt.storage.configuration.file.yaml.StringYamlConfigurationProvider;

class ConfigImportRunRecordTests {

    private FileConfigurationProvider provider;
    private ConfigImportRunRecord record;

    @BeforeEach
    void setUp() {
        provider = new StringYamlConfigurationProvider("");
        record = new ConfigImportRunRecord(provider);
    }

    @Test
    void isCompleted_returns_false_when_key_is_absent() {
        assertFalse(record.isCompleted("my-importer"));
    }

    @Test
    void isCompleted_returns_false_when_key_is_false() {
        provider = new StringYamlConfigurationProvider("is-importer-completed:\n  my-importer: false\n");
        record = new ConfigImportRunRecord(provider);

        assertFalse(record.isCompleted("my-importer"));
    }

    @Test
    void isCompleted_returns_true_when_key_is_true() {
        provider = new StringYamlConfigurationProvider("is-importer-completed:\n  my-importer: true\n");
        record = new ConfigImportRunRecord(provider);

        assertTrue(record.isCompleted("my-importer"));
    }

    @Test
    void markCompleted_sets_key_to_true_in_memory() {
        record.markCompleted("my-importer");

        assertTrue(record.isCompleted("my-importer"));
    }

    @Test
    void markCompleted_does_not_affect_other_importers() {
        record.markCompleted("importer-a");

        assertFalse(record.isCompleted("importer-b"));
    }
}
