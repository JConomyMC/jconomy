package com.jellyrekt.configuration.migration;

import org.bukkit.configuration.Configuration;

@FunctionalInterface
public interface ConfigMigration {
    void apply(Configuration config);
}
