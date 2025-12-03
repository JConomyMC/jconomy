package com.jellyrekt.jconomy;

import org.bukkit.plugin.java.JavaPlugin;

public class JConomy extends JavaPlugin {
    public static final int CONFIG_VERSION = 1;

    @Override
    public void onEnable() {
        ConfigUtils.runConfigMigrations(this);
    }
}
