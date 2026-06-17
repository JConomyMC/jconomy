package org.jconomy;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.AccountCache;
import org.jconomy.accounts.AccountRepository;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.accounts.BalanceCache;
import org.jconomy.accounts.BalanceRepository;
import org.jconomy.accounts.DefaultAccountAccess;
import org.jconomy.accounts.DefaultBalanceAccess;
import org.jconomy.accounts.LruAccountCache;
import org.jconomy.accounts.LruBalanceCache;
import org.jconomy.accounts.SqliteAccountRepository;
import org.jconomy.accounts.SqliteBalanceRepository;
import org.jconomy.adapters.BukkitPlayerResolver;
import org.jconomy.adapters.DefaultResponseMapper;
import org.jconomy.adapters.EconomyResponseMapper;
import org.jconomy.adapters.LegacyEconomyAdapter;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.admin.AccountCreateCommandHandler;
import org.jconomy.commands.admin.AccountDeleteCommandHandler;
import org.jconomy.commands.admin.BalanceAddCommandHandler;
import org.jconomy.commands.admin.BalanceGetCommandHandler;
import org.jconomy.commands.admin.BalanceRemoveCommandHandler;
import org.jconomy.commands.admin.BalanceSetCommandHandler;
import org.jconomy.config.CacheConfig;
import org.jconomy.config.DefaultCacheConfig;
import org.jconomy.config.DefaultFeatureManager;
import org.jconomy.config.DefaultJConomyConfig;
import org.jconomy.config.DefaultVaultLegacyAdapterConfig;
import org.jconomy.config.JConomyConfig;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.config.economy.YamlEconomyConfig;
import org.jconomy.dependencyinjection.DefaultServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.extensions.ExtensionManager;
import org.jconomy.listeners.PlayerJoinListener;
import org.jconomy.presentation.CurrencyFormatter;
import org.jconomy.presentation.DefaultCurrencyFormatter;
import org.jconomy.presentation.DefaultNumberFormatter;
import org.jconomy.presentation.NumberFormatter;
import org.jconomy.storage.DatabaseMigrator;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.storage.SqliteConnectionFactory;
import org.jconomy.storage.SqliteMigrator;
import com.jellyrekt.storage.configuration.file.FileConfigurationProvider;
import com.jellyrekt.storage.configuration.file.javaplugin.JavaPluginConfigurationProvider;

import net.milkbowl.vault2.economy.Economy;

public class JConomyServiceRegistrar {

    public static JConomyServiceProvider buildServiceProvider(
            JavaPlugin plugin, PluginContext pluginContext, ExtensionManager extensionManager) {
        var builder = new DefaultServiceBuilder();
        registerServices(builder, plugin, pluginContext, extensionManager);
        return builder.build();
    }

    private static void registerServices(
            DefaultServiceBuilder builder,
            JavaPlugin plugin, PluginContext pluginContext, ExtensionManager extensionManager) {
        builder.addSingleton(JavaPlugin.class, plugin);
        builder.addSingleton(PluginContext.class, pluginContext);
        builder.addSingleton(Logger.class, plugin.getLogger());
        builder.addSingleton(ConfigMigrator.class, DefaultConfigMigrator.class);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingletonFactory(CacheConfig.PeriodicFlushConfig.class, sp ->
                sp.getRequiredService(CacheConfig.class).getPeriodicFlushConfig());
        builder.addSingleton(PeriodicFlushScheduler.class);
        builder.addSingleton(FeatureManager.class, DefaultFeatureManager.class);
        builder.addSingleton(VaultLegacyAdapterConfig.class, DefaultVaultLegacyAdapterConfig.class);
        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(BalanceCache.class, LruBalanceCache.class);
        builder.addSingleton(EconomyConfig.class, YamlEconomyConfig.class);
        builder.addSingleton(NumberFormatter.class, DefaultNumberFormatter.class);
        builder.addSingleton(CurrencyFormatter.class, DefaultCurrencyFormatter.class);
        builder.addSingleton(SqlConnectionFactory.class,
                new SqliteConnectionFactory(plugin.getDataFolder().toPath().resolve("jconomy.db")));
        builder.addSingleton(DatabaseMigrator.class, SqliteMigrator.class);
        builder.addSingleton(AccountRepository.class, SqliteAccountRepository.class);
        builder.addSingleton(BalanceRepository.class, SqliteBalanceRepository.class);
        builder.addSingleton(AccountAccess.class, DefaultAccountAccess.class);
        builder.addSingleton(BalanceAccess.class, DefaultBalanceAccess.class);
        builder.addSingleton(FlushRegistry.class, DefaultFlushRegistry.class);
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
        extensionManager.configureServices(builder);
    }
}
