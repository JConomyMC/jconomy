package org.jconomy;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public class DefaultConfigMigrator implements ConfigMigrator {
    private final JavaPlugin plugin;

    public DefaultConfigMigrator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void migrate() {
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
        if (!config.contains("cache.lru-limit")) {
            config.set("cache.lru-limit", 10000);
        }

        setVersion(config, 1);
    }
}
