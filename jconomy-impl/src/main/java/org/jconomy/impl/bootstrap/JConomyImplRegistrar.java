package org.jconomy.impl.bootstrap;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.storage.SqliteConnectionFactory;
import org.jconomy.storage.DatabaseMigrator;
import org.jconomy.storage.SqliteMigrator;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.DefaultAccountAccess;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.accounts.DefaultBalanceAccess;
import org.jconomy.accounts.AccountRepository;
import org.jconomy.accounts.BalanceRepository;
import org.jconomy.accounts.SqliteAccountRepository;
import org.jconomy.accounts.SqliteBalanceRepository;
import org.jconomy.accounts.AccountCache;
import org.jconomy.accounts.BalanceCache;
import org.jconomy.accounts.LruAccountCache;
import org.jconomy.accounts.LruBalanceCache;
import org.jconomy.FeatureManager;
import org.jconomy.config.CacheConfig;
import org.jconomy.config.DefaultCacheConfig;
import org.jconomy.config.DefaultFeatureManager;

public final class JConomyImplRegistrar {

    private JConomyImplRegistrar() {
    }

    public static void registerServices(JConomyServiceBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder cannot be null");
        }

        builder.addSingleton(AccountCache.class, LruAccountCache.class);
        builder.addSingleton(BalanceCache.class, LruBalanceCache.class);
        builder.addSingleton(AccountRepository.class, SqliteAccountRepository.class);
        builder.addSingleton(BalanceRepository.class, SqliteBalanceRepository.class);
        builder.addSingleton(AccountAccess.class, DefaultAccountAccess.class);
        builder.addSingleton(BalanceAccess.class, DefaultBalanceAccess.class);
        builder.addSingleton(FlushRegistry.class, DefaultFlushRegistry.class);
        builder.addSingleton(SqlConnectionFactory.class, SqliteConnectionFactory.class);
        builder.addSingleton(DatabaseMigrator.class, SqliteMigrator.class);
        builder.addSingleton(CacheConfig.class, DefaultCacheConfig.class);
        builder.addSingleton(FeatureManager.class, DefaultFeatureManager.class);
    }
}
