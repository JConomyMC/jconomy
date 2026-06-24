package org.jconomy.impl.bootstrap;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.storage.SqliteConnectionFactory;
import org.jconomy.storage.DatabaseMigrator;
import org.jconomy.storage.SqliteMigrator;
import org.jconomy.accounts.AccountRepository;
import org.jconomy.balances.BalanceRepository;
import org.jconomy.accounts.SqliteAccountRepository;
import org.jconomy.balances.SqliteBalanceRepository;
import org.jconomy.FeatureManager;
import org.jconomy.config.DefaultFeatureManager;
import org.jconomy.config.DefaultVaultLegacyAdapterConfig;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.config.economy.YamlEconomyConfig;
import org.jconomy.presentation.CurrencyFormatter;
import org.jconomy.presentation.DefaultCurrencyFormatter;
import org.jconomy.presentation.DefaultNumberFormatter;
import org.jconomy.presentation.NumberFormatter;

public final class JConomyImplRegistrar {

    private JConomyImplRegistrar() {
    }

    public static void registerServices(JConomyServiceBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder cannot be null");
        }

        builder.addSingleton(AccountRepository.class, SqliteAccountRepository.class);
        builder.addSingleton(BalanceRepository.class, SqliteBalanceRepository.class);
        builder.addSingleton(FlushRegistry.class, DefaultFlushRegistry.class);
        builder.addSingleton(SqlConnectionFactory.class, SqliteConnectionFactory.class);
        builder.addSingleton(DatabaseMigrator.class, SqliteMigrator.class);
        builder.addSingleton(FeatureManager.class, DefaultFeatureManager.class);
        builder.addSingleton(VaultLegacyAdapterConfig.class, DefaultVaultLegacyAdapterConfig.class);
        builder.addSingleton(EconomyConfig.class, YamlEconomyConfig.class);
        builder.addSingleton(NumberFormatter.class, DefaultNumberFormatter.class);
        builder.addSingleton(CurrencyFormatter.class, DefaultCurrencyFormatter.class);
    }
}
