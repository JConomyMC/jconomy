package com.jellyrekt.jconomy;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.incendo.cloud.paper.LegacyPaperCommandManager;

import com.jellyrekt.jconomy.commands.CommandManagerFactory;
import com.jellyrekt.jconomy.commands.transfer.TransferCommandRegistrar;
import com.jellyrekt.jconomy.commands.transfer.TransferPlanStore;
import com.jellyrekt.jconomy.dependencyinjection.DefaultServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;
import com.jellyrekt.jconomy.expansions.DefaultExpansionLoader;
import com.jellyrekt.jconomy.expansions.DefaultExpansionManager;
import com.jellyrekt.jconomy.expansions.ExpansionManager;
import com.jellyrekt.jconomy.listeners.PlayerJoinListener;
import com.jellyrekt.jconomy.storage.DatabaseMigrator;
import com.jellyrekt.jconomy.storage.Flushable;
import com.jellyrekt.jconomy.transfer.TransferExporter;
import com.jellyrekt.jconomy.transfer.TransferImporter;

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerCommands() {
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
