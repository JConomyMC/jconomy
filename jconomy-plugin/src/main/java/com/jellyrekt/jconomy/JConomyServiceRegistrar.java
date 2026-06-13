package com.jellyrekt.jconomy;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.jellyrekt.jconomy.accounts.AccountAccess;
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
import com.jellyrekt.jconomy.config.CacheConfig;
import com.jellyrekt.jconomy.config.DefaultCacheConfig;
import com.jellyrekt.jconomy.config.DefaultJConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.economy.EconomyConfig;
import com.jellyrekt.jconomy.config.economy.YamlEconomyConfig;
import com.jellyrekt.jconomy.dependencyinjection.DefaultServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;
import com.jellyrekt.jconomy.expansions.ExpansionManager;
import com.jellyrekt.jconomy.listeners.PlayerJoinListener;
import com.jellyrekt.jconomy.presentation.CurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultCurrencyFormatter;
import com.jellyrekt.jconomy.presentation.DefaultNumberFormatter;
import com.jellyrekt.jconomy.presentation.NumberFormatter;
import com.jellyrekt.jconomy.storage.DatabaseMigrator;
import com.jellyrekt.jconomy.storage.Flushable;
import com.jellyrekt.jconomy.storage.SqlConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteConnectionFactory;
import com.jellyrekt.jconomy.storage.SqliteMigrator;
import com.jellyrekt.storage.configuration.file.FileConfigurationProvider;
import com.jellyrekt.storage.configuration.file.javaplugin.JavaPluginConfigurationProvider;

import net.milkbowl.vault2.economy.Economy;

public class JConomyServiceRegistrar {

    public static JConomyServiceProvider buildServiceProvider(
            JavaPlugin plugin, PluginContext pluginContext, ExpansionManager expansionManager) {
        var builder = new DefaultServiceBuilder();
        registerServices(builder, plugin, pluginContext, expansionManager);
        return builder.build();
    }

    private static void registerServices(
            DefaultServiceBuilder builder,
            JavaPlugin plugin, PluginContext pluginContext, ExpansionManager expansionManager) {
        builder.addSingleton(JavaPlugin.class, plugin);
        builder.addSingleton(PluginContext.class, pluginContext);
        builder.addSingleton(Logger.class, plugin.getLogger());
        builder.addSingleton(ConfigMigrator.class, DefaultConfigMigrator.class);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(EconomyConfig.class, YamlEconomyConfig.class);
        builder.addSingleton(NumberFormatter.class, DefaultNumberFormatter.class);
        builder.addSingleton(CurrencyFormatter.class, DefaultCurrencyFormatter.class);
        builder.addSingleton(SqlConnectionFactory.class,
                new SqliteConnectionFactory(plugin.getDataFolder().toPath().resolve("jconomy.db")));
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
        expansionManager.configureServices(builder);
    }
}
