package com.jellyrekt.jconomy;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigUtils {
    public static void runConfigMigrations(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        var config = plugin.getConfig();

        if (getVersion(config) < 1)
            upgradeToVersion1(config);

        plugin.saveConfig();
    }
    
    private static int getVersion(Configuration config) {
        return config.getInt("config-version", 0);
    }

    private static void setVersion(Configuration config, int version) {
        config.set("config-version", version);
    }
    
    private static void upgradeToVersion1(Configuration config) {
        // TODO:
        // For each value that SHOULD exist in version 1,
        // if it does NOT exist, set its default value.
        
        setVersion(config, 1);
    }
}
