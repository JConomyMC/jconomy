package com.jellyrekt.configuration.migration;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigMigrationTests {

    @Test
    void apply_mutates_configuration() {
        var config = new MemoryConfiguration();
        ConfigMigration migration = c -> c.set("test-key", "test-value");

        migration.apply(config);

        assertEquals("test-value", config.getString("test-key"));
    }

}
