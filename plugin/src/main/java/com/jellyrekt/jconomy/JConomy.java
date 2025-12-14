package com.jellyrekt.jconomy;

import java.nio.file.Path;

import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.accounts.AccountCache;
import com.jellyrekt.jconomy.accounts.LruAccountCache;
import com.jellyrekt.jconomy.accounts.AccountAccess;
import com.jellyrekt.jconomy.config.CacheConfig;
import com.jellyrekt.jconomy.config.DefaultCacheConfig;
import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.YamlJConomyConfig;
import com.jellyrekt.jconomy.presentation.CurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultCurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultNumberFormatter;
import com.jellyrekt.jconomy.presentation.NumberFormatter;
import com.jellyrekt.jconomy.storage.DatabaseMigrator;
import com.jellyrekt.jconomy.storage.SqlConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteMigrator;
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
        //
    }

    private void configureServices() throws Exception {
        var builder = new ServiceBuilder();

        builder.addSingleton(JavaPlugin.class, this);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(JConomyConfig.class, YamlJConomyConfig.class);
        builder.addSingleton(NumberFormatter.class, DefaultNumberFormatter.class);
        builder.addSingleton(CurrencyFormatter.class, DefaultCurrencyFormatter.class);
        builder.addSingleton(SqlConnectionFactory.class,
                new SqliteConnectionFactory(getDataFolder().toPath().resolve("jconomy.db")));
        builder.addSingleton(DatabaseMigrator.class, SqliteMigrator.class);

        services = builder.build(true);
    }
}
