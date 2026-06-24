package com.jellyrekt.configuration.migration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigMigratorTests {

    @Test
    void builder_builds_a_config_migrator() {
        var migrator = ConfigMigrator.builder("config-version").build();

        assertNotNull(migrator);
    }

    @Test
    void addNext_supports_fluent_chaining() {
        var builder = ConfigMigrator.builder("config-version");

        var result = builder.addNext(config -> {});

        assertSame(builder, result);
    }
}
