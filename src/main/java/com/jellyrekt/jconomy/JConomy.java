package com.jellyrekt.jconomy;

import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.balances.cache.BalanceCache;
import com.jellyrekt.jconomy.balances.cache.LruBalanceCache;
import com.jellyrekt.jconomy.config.CacheConfig;
import com.jellyrekt.jconomy.config.DefaultCacheConfig;
import com.jellyrekt.jconomy.listeners.PlayerJoinListener;
import com.jellyrekt.jconomy.storage.ConfigFileConfigurationProvider;
import com.merenze.dependencyinjection.ServiceBuilder;
import com.merenze.dependencyinjection.ServiceProvider;

public class JConomy extends JavaPlugin {
    public static final int CONFIG_VERSION = 1;

    private ServiceProvider services;

    @Override
    public void onEnable() {
        ConfigUtils.runConfigMigrations(this);

        try {
            configureServices();
        } catch (Exception ex) {
            getLogger().severe("Some services could not be instantiated: " + ex.getMessage());
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerEvents();
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(services.getRequiredService(PlayerJoinListener.class), this);
    }

    private void configureServices() throws Exception {
        var builder = new ServiceBuilder();

        builder.addSingleton(JavaPlugin.class, this);
        builder.addSingleton(ConfigFileConfigurationProvider.class, new ConfigFileConfigurationProvider(getDataFolder(), "config.yml"));
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(BalanceCache.class, LruBalanceCache.class);
        // TODO add BalanceRepository
        builder.addSingleton(PlayerJoinListener.class);

        services = builder.build();
    }
}
