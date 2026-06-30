package org.jconomy;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.jellyrekt.configuration.migration.ConfigMigrator;

import org.jconomy.commands.CommandManagerFactory;
import org.jconomy.config.migrations.ConfigMigrationV1;
import org.jconomy.commands.admin.AccountCommandRegistrar;
import org.jconomy.commands.admin.BalanceCommandRegistrar;
import org.jconomy.commands.transfer.TransferCommandRegistrar;
import org.jconomy.commands.transfer.TransferPlanStore;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.extensions.DefaultExtensionLoader;
import org.jconomy.extensions.DefaultExtensionManager;
import org.jconomy.extensions.ExtensionManager;
import org.jconomy.listeners.PlayerJoinListener;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.config.migrations.*;
import org.jconomy.balances.BalanceAccess;
import org.jconomy.config.CacheConfig;
import org.jconomy.storage.Flushable;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.jconomy.storage.SqliteMigrator;
import org.jconomy.transfer.TransferExporter;
import org.jconomy.transfer.TransferImporter;

import net.milkbowl.vault2.economy.Economy;

public class JConomy extends JavaPlugin implements PluginContext {
    public static final int CONFIG_VERSION = 1;
    private final ExtensionManager extensionManager = createExtensionManager();

    private JConomyServiceProvider services;
    private PeriodicFlushScheduler periodicFlushScheduler;

    private ExtensionManager createExtensionManager() {
        var loader = new DefaultExtensionLoader(this, getClassLoader());
        return new DefaultExtensionManager(loader, getLogger());
    }

    @Override
    public void onEnable() {
        if (!isVaultUnlockedAPILoaded()) {
            getLogger().severe("VaultUnlockedAPI is not loaded. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            services = JConomyServiceRegistrar.buildServiceProvider(this, this, extensionManager);
            // Create scheduler outside DI container to prevent extension access/override
            periodicFlushScheduler = JConomyServiceRegistrar.createPeriodicFlushScheduler(services);
        } catch (Exception ex) {
            getLogger().severe("Some services could not be instantiated: " + ExceptionUtils.getStackTrace(ex));
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        buildConfigMigrator().migrate(getConfig());
        saveConfig();
        services.getRequiredService(SqliteMigrator.class).migrate();
        registerFlushables();
        startPeriodicFlushIfEnabled();
        extensionManager.notifyServicesReady(services);
        registerServices();
        registerEvents();
        registerCommands();
    }

    private static ConfigMigrator buildConfigMigrator() {
        return ConfigMigrator.builder("config-version")
                .addNext(new ConfigMigrationV1())
                .build();
    }

    private void registerFlushables() {
        var registry = services.getRequiredService(FlushRegistry.class);
        var balanceAccess = services.getRequiredService(BalanceAccess.class);
        if (balanceAccess instanceof Flushable f) registry.register(f);
    }

    private boolean isVaultUnlockedAPILoaded() {
        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void startPeriodicFlushIfEnabled() {
        if (services.getRequiredService(CacheConfig.PeriodicFlushConfig.class).isEnabled()) {
            periodicFlushScheduler.start();
        }
    }

    @Override
    public void onDisable() {
        periodicFlushScheduler.stop();
        services.getRequiredService(FlushRegistry.class).flushAll();

        extensionManager.close();
    }
    
    private void registerServices() {
        getServer().getServicesManager().register(
                Economy.class,
                services.getRequiredService(Economy.class),
                this,
                ServicePriority.Normal);

        var legacyAdapterConfig = services.getRequiredService(VaultLegacyAdapterConfig.class);
        if (legacyAdapterConfig.isEnabled()) {
            getServer().getServicesManager().register(
                    net.milkbowl.vault.economy.Economy.class,
                    services.getRequiredService(net.milkbowl.vault.economy.Economy.class),
                    this,
                    ServicePriority.Normal);
        }
    }
    
    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(services.getRequiredService(PlayerJoinListener.class), this);
    }

    private void registerCommands() {
        services.getRequiredService(BalanceCommandRegistrar.class).register();
        services.getRequiredService(AccountCommandRegistrar.class).register();

        // TODO: migrate TransferCommandRegistrar into the service provider (same pattern as
        // BalanceCommandRegistrar / AccountCommandRegistrar). Deferred — data transfer is
        // not planned for release 1.0.
        if (services.getRequiredService(FeatureManager.class).isEnabled(FeatureNames.DATA_TRANSFER)) {
            var commandManager = new CommandManagerFactory(this).create();
            var importers = services.getServices(TransferImporter.class);
            var exporters = services.getServices(TransferExporter.class);
            new TransferCommandRegistrar(
                    commandManager,
                    importers,
                    exporters,
                    services.getRequiredService(BukkitScheduler.class),
                    this,
                    new TransferPlanStore()
            ).register();
        }
    }

}
