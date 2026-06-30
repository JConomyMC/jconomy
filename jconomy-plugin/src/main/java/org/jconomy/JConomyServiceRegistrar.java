package org.jconomy;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jconomy.adapters.BukkitPlayerResolver;
import org.jconomy.adapters.DefaultResponseMapper;
import org.jconomy.adapters.EconomyResponseMapper;
import org.jconomy.adapters.LegacyEconomyAdapter;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.admin.AccountCommandRegistrar;
import org.jconomy.commands.admin.AccountCreateCommandHandler;
import org.jconomy.commands.admin.AccountDeleteCommandHandler;
import org.jconomy.commands.admin.BalanceAddCommandHandler;
import org.jconomy.commands.admin.BalanceCommandRegistrar;
import org.jconomy.commands.admin.BalanceGetCommandHandler;
import org.jconomy.commands.admin.BalanceRemoveCommandHandler;
import org.jconomy.commands.admin.BalanceSetCommandHandler;
import org.jconomy.commands.CommandManagerFactory;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.AccountCache;
import org.jconomy.accounts.DefaultAccountAccess;
import org.jconomy.accounts.LruAccountCache;
import org.jconomy.balances.BalanceAccess;
import org.jconomy.balances.BalanceCache;
import org.jconomy.balances.DefaultBalanceAccess;
import org.jconomy.balances.LruBalanceCache;
import org.jconomy.config.CacheConfig;
import org.jconomy.config.DefaultCacheConfig;
import org.jconomy.config.DefaultJConomyConfig;
import org.jconomy.config.JConomyConfig;
import org.jconomy.dependencyinjection.DefaultServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.extensions.ExtensionManager;
import org.jconomy.impl.bootstrap.JConomyImplRegistrar;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jconomy.listeners.PlayerJoinListener;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.jconomy.storage.SqliteMigrator;

import com.jellyrekt.storage.configuration.file.FileConfigurationProvider;
import com.jellyrekt.storage.configuration.file.javaplugin.JavaPluginConfigurationProvider;

import net.milkbowl.vault2.economy.Economy;

public class JConomyServiceRegistrar {

    public static JConomyServiceProvider buildServiceProvider(
            JavaPlugin plugin, PluginContext pluginContext, ExtensionManager extensionManager) {
        var builder = new DefaultServiceBuilder();
        registerServices(builder, plugin, pluginContext, extensionManager);
        // Re-register protected infrastructure after extensions have configured their services.
        // This prevents extensions from overriding critical services like FlushRegistry.
        registerProtectedInfrastructure(builder);
        return builder.build();
    }

    private static void registerServices(
            DefaultServiceBuilder builder,
            JavaPlugin plugin, PluginContext pluginContext, ExtensionManager extensionManager) {
        builder.addSingleton(JavaPlugin.class, plugin);
        builder.addSingleton(PluginContext.class, pluginContext);
        builder.addSingleton(Logger.class, plugin.getLogger());
        JConomyImplRegistrar.registerServices(builder);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(BalanceCache.class, LruBalanceCache.class);
        builder.addSingleton(AccountAccess.class, DefaultAccountAccess.class);
        builder.addSingleton(BalanceAccess.class, DefaultBalanceAccess.class);
        builder.addSingleton(Economy.class, EconomyImp.class);
        builder.addSingleton(EconomyResponseMapper.class, DefaultResponseMapper.class);
        builder.addSingleton(PlayerResolver.class, BukkitPlayerResolver.class);
        builder.addSingleton(net.milkbowl.vault.economy.Economy.class, LegacyEconomyAdapter.class);
        builder.addSingletonFactory(BukkitScheduler.class, sp ->
                sp.getRequiredService(JavaPlugin.class).getServer().getScheduler());
        builder.addSingleton(PlayerJoinListener.class);
        builder.addSingleton(BalanceGetCommandHandler.class);
        builder.addSingleton(BalanceSetCommandHandler.class);
        builder.addSingleton(BalanceAddCommandHandler.class);
        builder.addSingleton(BalanceRemoveCommandHandler.class);
        builder.addSingleton(AccountCreateCommandHandler.class);
        builder.addSingleton(AccountDeleteCommandHandler.class);
        builder.addSingletonFactory(LegacyPaperCommandManager.class, sp ->
                new CommandManagerFactory(sp.getRequiredService(JavaPlugin.class)).create());
        builder.addSingletonFactory(BalanceCommandRegistrar.class, sp -> new BalanceCommandRegistrar(
                sp.getRequiredService(LegacyPaperCommandManager.class),
                sp.getRequiredService(BalanceGetCommandHandler.class),
                sp.getRequiredService(BalanceSetCommandHandler.class),
                sp.getRequiredService(BalanceAddCommandHandler.class),
                sp.getRequiredService(BalanceRemoveCommandHandler.class)));
        builder.addSingletonFactory(AccountCommandRegistrar.class, sp -> new AccountCommandRegistrar(
                sp.getRequiredService(LegacyPaperCommandManager.class),
                sp.getRequiredService(AccountCreateCommandHandler.class),
                sp.getRequiredService(AccountDeleteCommandHandler.class)));
        builder.addSingletonFactory(JConomyConfig.class, sp -> {
            var javaPlugin = sp.getRequiredService(JavaPlugin.class);
            return new DefaultJConomyConfig(() -> javaPlugin.getConfig());
        });
        builder.addSingletonFactory(FileConfigurationProvider.class, sp -> {
            try {
                return new JavaPluginConfigurationProvider(sp.getRequiredService(JavaPlugin.class));
            } catch (Exception e) {
                throw new RuntimeException("Failed to create config provider", e);
            }
        });
        builder.addSingleton(SqliteMigrator.class);
        extensionManager.configureServices(builder);
    }

    /**
     * Registers critical infrastructure services that should not be overridable by extensions.
     * This method is called after extensions have configured their services.
     * <p>
     * Protected services:
     * <ul>
    *   <li>{@link FlushRegistry} - Registered here after extensions to prevent override</li>
     *   <li>{@link CacheConfig.PeriodicFlushConfig} - Configuration for periodic flushing (re-registered to ensure it's set)</li>
     * </ul>
     * </p>
     */
    private static void registerProtectedInfrastructure(DefaultServiceBuilder builder) {
        builder.addSingleton(FlushRegistry.class, DefaultFlushRegistry.class);
        // Re-register PeriodicFlushConfig factory to ensure it's available with correct dependencies
        builder.addSingletonFactory(CacheConfig.PeriodicFlushConfig.class, sp ->
                sp.getRequiredService(CacheConfig.class).getPeriodicFlushConfig());
    }

    /**
     * Creates a {@link PeriodicFlushScheduler} instance outside the DI container.
     * This scheduler is not registered in the service provider, preventing extensions
     * from accessing or replacing it.
     *
     * @param provider the built service provider
     * @return a new {@link PeriodicFlushScheduler} instance
     */
    public static PeriodicFlushScheduler createPeriodicFlushScheduler(JConomyServiceProvider provider) {
        return new PeriodicFlushScheduler(
                provider.getRequiredService(JavaPlugin.class),
                provider.getRequiredService(BukkitScheduler.class),
                provider.getRequiredService(FlushRegistry.class),
                provider.getRequiredService(CacheConfig.PeriodicFlushConfig.class));
    }
}
