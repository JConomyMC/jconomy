package org.jconomy;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.CommandManagerFactory;
import org.jconomy.commands.admin.AdminCommandRegistrar;
import org.jconomy.commands.transfer.TransferCommandRegistrar;
import org.jconomy.commands.transfer.TransferPlanStore;
import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.extensions.DefaultExtensionLoader;
import org.jconomy.extensions.DefaultExtensionManager;
import org.jconomy.extensions.ExtensionManager;
import org.jconomy.listeners.PlayerJoinListener;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.config.CacheConfig;
import org.jconomy.storage.DatabaseMigrator;
import org.jconomy.storage.Flushable;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.jconomy.transfer.TransferExporter;
import org.jconomy.transfer.TransferImporter;

import net.milkbowl.vault2.economy.Economy;

public class JConomy extends JavaPlugin implements PluginContext {
    public static final int CONFIG_VERSION = 1;
    private final ExtensionManager extensionManager = createExtensionManager();

    private JConomyServiceProvider services;

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
        } catch (Exception ex) {
            getLogger().severe("Some services could not be instantiated: " + ExceptionUtils.getStackTrace(ex));
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        services.getRequiredService(ConfigMigrator.class).migrate();
        services.getRequiredService(DatabaseMigrator.class).migrate();
        registerFlushables();
        startPeriodicFlushIfEnabled();
        extensionManager.notifyServicesReady(services);
        registerServices();
        registerEvents();
        registerCommands();
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
            services.getRequiredService(PeriodicFlushScheduler.class).start();
        }
    }

    @Override
    public void onDisable() {
        services.getRequiredService(PeriodicFlushScheduler.class).stop();
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
        var commandManager = new CommandManagerFactory(this).create();

        new AdminCommandRegistrar(
                commandManager,
                services.getRequiredService(BalanceAccess.class),
                services.getRequiredService(AccountAccess.class),
                services.getRequiredService(EconomyConfig.class),
                services.getRequiredService(PlayerResolver.class)
        ).register();

        if (services.getRequiredService(FeatureManager.class).isEnabled(FeatureNames.DATA_TRANSFER)) {
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
