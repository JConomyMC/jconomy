package com.jellyrekt.configuration.migration;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void migrate_does_not_apply_any_migrations_when_config_is_current() {
        var config = new MemoryConfiguration();
        config.set("config-version", 2);
        var applied = new boolean[]{false, false};
        var migrator = ConfigMigrator.builder("config-version")
                .addNext(c -> applied[0] = true)
                .addNext(c -> applied[1] = true)
                .build();

        migrator.migrate(config);

        assertFalse(applied[0]);
        assertFalse(applied[1]);
    }

    @Test
    void migrate_applies_all_pending_migrations_and_updates_version() {
        var config = new MemoryConfiguration();
        var callCount = new int[]{0};
        var migrator = ConfigMigrator.builder("config-version")
                .addNext(c -> callCount[0]++)
                .addNext(c -> callCount[0]++)
                .build();

        migrator.migrate(config);

        assertEquals(2, callCount[0]);
        assertEquals(2, config.getInt("config-version"));
    }

    @Test
    void migrate_skips_migrations_already_applied() {
        var config = new MemoryConfiguration();
        config.set("config-version", 1);
        var firstApplied = new boolean[]{false};
        var secondApplied = new boolean[]{false};
        var migrator = ConfigMigrator.builder("config-version")
                .addNext(c -> firstApplied[0] = true)
                .addNext(c -> secondApplied[0] = true)
                .build();

        migrator.migrate(config);

        assertFalse(firstApplied[0]);
        assertTrue(secondApplied[0]);
        assertEquals(2, config.getInt("config-version"));
    }

    @Test
    void migrate_updates_version_after_each_migration() {
        var config = new MemoryConfiguration();
        var versionSeenBySecondMigration = new int[]{-1};
        var migrator = ConfigMigrator.builder("config-version")
                .addNext(c -> {})
                .addNext(c -> versionSeenBySecondMigration[0] = c.getInt("config-version", 0))
                .build();

        migrator.migrate(config);

        assertEquals(1, versionSeenBySecondMigration[0]);
    }
}

