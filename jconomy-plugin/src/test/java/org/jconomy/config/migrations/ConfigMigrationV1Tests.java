package org.jconomy.config.migrations;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigMigrationV1Tests {

    private final ConfigMigrationV1 migration = new ConfigMigrationV1();

    @Test
    void apply_sets_lru_limit_when_absent() {
        var config = new MemoryConfiguration();

        migration.apply(config);

        assertEquals(10000, config.getInt("cache.lru-limit"));
    }

    @Test
    void apply_does_not_overwrite_existing_lru_limit() {
        var config = new MemoryConfiguration();
        config.set("cache.lru-limit", 500);

        migration.apply(config);

        assertEquals(500, config.getInt("cache.lru-limit"));
    }

    @Test
    void apply_sets_periodic_flush_enabled_when_absent() {
        var config = new MemoryConfiguration();

        migration.apply(config);

        assertEquals(true, config.getBoolean("cache.periodic-flush.enabled"));
    }

    @Test
    void apply_does_not_overwrite_existing_periodic_flush_enabled() {
        var config = new MemoryConfiguration();
        config.set("cache.periodic-flush.enabled", false);

        migration.apply(config);

        assertEquals(false, config.getBoolean("cache.periodic-flush.enabled"));
    }

    @Test
    void apply_sets_periodic_flush_interval_ticks_when_absent() {
        var config = new MemoryConfiguration();

        migration.apply(config);

        assertEquals(1200, config.getInt("cache.periodic-flush.interval-ticks"));
    }

    @Test
    void apply_does_not_overwrite_existing_periodic_flush_interval_ticks() {
        var config = new MemoryConfiguration();
        config.set("cache.periodic-flush.interval-ticks", 100);

        migration.apply(config);

        assertEquals(100, config.getInt("cache.periodic-flush.interval-ticks"));
    }
}
