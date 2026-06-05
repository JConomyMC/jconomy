package com.jellyrekt.jconomy;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.accounts.AccountCache;
import com.jellyrekt.jconomy.accounts.AccountNameAccess;
import com.jellyrekt.jconomy.accounts.AccountNameCache;
import com.jellyrekt.jconomy.accounts.AccountNameRepository;
import com.jellyrekt.jconomy.accounts.AccountRepository;
import com.jellyrekt.jconomy.accounts.DefaultAccountAccess;
import com.jellyrekt.jconomy.accounts.DefaultAccountNameAccess;
import com.jellyrekt.jconomy.accounts.LruAccountCache;
import com.jellyrekt.jconomy.accounts.LruAccountNameCache;
import com.jellyrekt.jconomy.accounts.SqliteAccountNameRepository;
import com.jellyrekt.jconomy.accounts.SqliteAccountRepository;
import com.jellyrekt.jconomy.adapters.BukkitPlayerResolver;
import com.jellyrekt.jconomy.adapters.DefaultResponseMapper;
import com.jellyrekt.jconomy.adapters.EconomyResponseMapper;
import com.jellyrekt.jconomy.adapters.LegacyEconomyAdapter;
import com.jellyrekt.jconomy.adapters.PlayerResolver;
import com.jellyrekt.jconomy.accounts.AccountAccess;
import com.jellyrekt.jconomy.config.CacheConfig;
import com.jellyrekt.jconomy.config.DefaultCacheConfig;
import com.jellyrekt.jconomy.config.DefaultJConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.economy.EconomyConfig;
import com.jellyrekt.jconomy.config.economy.YamlEconomyConfig;
import com.jellyrekt.jconomy.dependencyinjection.DefaultServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;
import com.jellyrekt.jconomy.expansions.DefaultExpansionLoader;
import com.jellyrekt.jconomy.expansions.DefaultExpansionManager;
import com.jellyrekt.jconomy.expansions.ExpansionManager;
import com.jellyrekt.jconomy.listeners.PlayerJoinListener;
import com.jellyrekt.jconomy.presentation.CurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultCurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultNumberFormatter;
import com.jellyrekt.jconomy.presentation.NumberFormatter;
import com.jellyrekt.jconomy.storage.DataImporter;
import com.jellyrekt.jconomy.storage.DatabaseMigrator;
import com.jellyrekt.jconomy.storage.Flushable;
import com.jellyrekt.jconomy.storage.SqlConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteMigrator;

import net.milkbowl.vault2.economy.Economy;

import org.bukkit.scheduler.BukkitScheduler;

public class JConomy extends JavaPlugin implements PluginContext {
    public static final int CONFIG_VERSION = 1;
    private final ExpansionManager expansionManager = createExpansionManager();

    private JConomyServiceProvider services;

    private ExpansionManager createExpansionManager() {
        var loader = new DefaultExpansionLoader(this, getClassLoader());
        return new DefaultExpansionManager(loader, getLogger());
    }

    @Override
    public void onEnable() {
        if (!isVaultUnlockedAPILoaded()) {
            getLogger().severe("VaultUnlockedAPI is not loaded. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            configureServices();
        } catch (Exception ex) {
            getLogger().severe("Some services could not be instantiated: " + ExceptionUtils.getStackTrace(ex));
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        services.getRequiredService(ConfigMigrator.class).migrate();
        importData();
        registerServices();

        registerEvents();
    }

    private boolean isVaultUnlockedAPILoaded() {
        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        services.getServices(Flushable.class).forEach(Flushable::flush);

        expansionManager.close();
    }
    
    private void registerServices() {
        getServer().getServicesManager().register(
                Economy.class,
                services.getRequiredService(Economy.class),
                this,
                ServicePriority.Normal);
                
        getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                services.getRequiredService(net.milkbowl.vault.economy.Economy.class),
                this,
                ServicePriority.Normal);
    }
    
    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(services.getRequiredService(PlayerJoinListener.class), this);
    }

    private void importData() {
        services.getServices(DataImporter.class).forEach(importer -> {
            try {
                importer.importData();
                getLogger().info(String.format("Imported data with " + importer.getClass().getName()));
            } catch (Exception ex) {
                getLogger().warning(String.format("Data import failed for '%s': %s", importer.getClass().getName(),
                        ex.getMessage()));
            }
        });
    }

    private void configureServices() throws Exception {
        var builder = new DefaultServiceBuilder();

        builder.addSingleton(JavaPlugin.class, this);
        builder.addSingleton(PluginContext.class, this);
        builder.addSingleton(java.util.logging.Logger.class, getLogger());
        builder.addSingleton(ConfigMigrator.class, DefaultConfigMigrator.class);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(EconomyConfig.class, YamlEconomyConfig.class);
        builder.addSingleton(NumberFormatter.class, DefaultNumberFormatter.class);
        builder.addSingleton(CurrencyFormatter.class, DefaultCurrencyFormatter.class);
        builder.addSingleton(SqlConnectionFactory.class,
                new SqliteConnectionFactory(getDataFolder().toPath().resolve("jconomy.db")));
        builder.addSingleton(DatabaseMigrator.class, SqliteMigrator.class);
        builder.addSingleton(AccountRepository.class, SqliteAccountRepository.class);
        builder.addSingleton(AccountAccess.class, DefaultAccountAccess.class);
        builder.addSingletonFactory(Flushable.class, sp -> (Flushable) sp.getRequiredService(AccountAccess.class));
        builder.addSingleton(Economy.class, EconomyImp.class);
        builder.addSingleton(EconomyResponseMapper.class, DefaultResponseMapper.class);
        builder.addSingleton(PlayerResolver.class, BukkitPlayerResolver.class);
        builder.addSingleton(net.milkbowl.vault.economy.Economy.class, LegacyEconomyAdapter.class);
        builder.addSingletonFactory(BukkitScheduler.class, sp ->
                sp.getRequiredService(JavaPlugin.class).getServer().getScheduler());
        builder.addSingleton(PlayerJoinListener.class);
        builder.addSingleton(AccountNameCache.class, LruAccountNameCache.class);
        builder.addSingleton(AccountNameRepository.class, SqliteAccountNameRepository.class);
        builder.addSingleton(AccountNameAccess.class, DefaultAccountNameAccess.class);
        builder.addSingletonFactory(Flushable.class, sp -> (Flushable) sp.getRequiredService(AccountNameAccess.class));
        builder.addSingletonFactory(JConomyConfig.class, serviceProvider -> {
            var plugin = serviceProvider.getRequiredService(JavaPlugin.class);
            return new DefaultJConomyConfig(() -> plugin.getConfig());
        });

        expansionManager.configureServices(builder);

        services = builder.build();
    }
}
