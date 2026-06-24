package org.jconomy.config.migrations;

import com.jellyrekt.configuration.migration.ConfigMigration;

public class ConfigMigrationV1 implements ConfigMigration {
    @Override
    public void apply(org.bukkit.configuration.Configuration config) {
        if (!config.contains("cache.lru-limit")) {
            config.set("cache.lru-limit", 10000);
        }

        if (!config.contains("cache.periodic-flush.enabled")) {
            config.set("cache.periodic-flush.enabled", true);
        }

        if (!config.contains("cache.periodic-flush.interval-ticks")) {
            config.set("cache.periodic-flush.interval-ticks", 1200);
        }
    }
    
}
