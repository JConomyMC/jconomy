package org.jconomy;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.incendo.cloud.paper.LegacyPaperCommandManager;

import org.jconomy.commands.CommandManagerFactory;
import org.jconomy.commands.transfer.TransferCommandRegistrar;
import org.jconomy.commands.transfer.TransferPlanStore;
import org.jconomy.FeatureManager;
import org.jconomy.FeatureNames;
import org.jconomy.dependencyinjection.DefaultServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.expansions.DefaultExpansionLoader;
import org.jconomy.expansions.DefaultExpansionManager;
import org.jconomy.expansions.ExpansionManager;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.listeners.PlayerJoinListener;
import org.jconomy.storage.DatabaseMigrator;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.transfer.TransferExporter;
import org.jconomy.transfer.TransferImporter;

import net.milkbowl.vault2.economy.Economy;

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
            services = JConomyServiceRegistrar.buildServiceProvider(this, this, expansionManager);
        } catch (Exception ex) {
            getLogger().severe("Some services could not be instantiated: " + ExceptionUtils.getStackTrace(ex));
            getLogger().severe("Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        services.getRequiredService(ConfigMigrator.class).migrate();
        services.getRequiredService(DatabaseMigrator.class).migrate();
        expansionManager.notifyServicesReady(services);
        registerServices();
        registerEvents();
        registerCommands();
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
        services.getRequiredService(FlushRegistry.class).flushAll();

        expansionManager.close();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerCommands() {
        if (!services.getRequiredService(FeatureManager.class).isEnabled(FeatureNames.DATA_TRANSFER)) {
            return;
        }

        var commandServiceBuilder = new DefaultServiceBuilder();
        commandServiceBuilder.addSingletonFactory(
                (Class) LegacyPaperCommandManager.class,
                sp -> new CommandManagerFactory(this).create());
        commandServiceBuilder.addSingletonFactory(TransferCommandRegistrar.class, sp -> {
            var importers = services.getServices(TransferImporter.class);
            var exporters = services.getServices(TransferExporter.class);
            return new TransferCommandRegistrar(
                    sp.getRequiredService(LegacyPaperCommandManager.class),
                    importers,
                    exporters,
                    services.getRequiredService(BukkitScheduler.class),
                    this,
                    new TransferPlanStore());
        });
        commandServiceBuilder.build().getRequiredService(TransferCommandRegistrar.class).register();
    }

}
