package org.jconomy.impl.bootstrap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.jconomy.dependencyinjection.JConomyServiceRegistrar;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.accounts.AccountRepository;
import org.jconomy.accounts.SqliteAccountRepository;
import org.jconomy.balances.BalanceRepository;
import org.jconomy.balances.SqliteBalanceRepository;
import org.jconomy.storage.SqlConnectionFactory;
import org.jconomy.FeatureManager;
import org.jconomy.config.DefaultVaultLegacyAdapterConfig;
import org.jconomy.config.VaultLegacyAdapterConfig;
import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.config.economy.YamlEconomyConfig;
import org.jconomy.presentation.CurrencyFormatter;
import org.jconomy.presentation.DefaultCurrencyFormatter;
import org.jconomy.presentation.DefaultNumberFormatter;
import org.jconomy.presentation.NumberFormatter;

class JConomyImplRegistrarTests {

    @Test
    void registerServices_accepts_builder() {
        var builder = new NoopBuilder();

        assertDoesNotThrow(() -> JConomyImplRegistrar.registerServices(builder));
    }

    @Test
    void registerServices_does_not_register_flush_registry() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(!builder.singletonImplementations.containsKey(FlushRegistry.class));
    }

    @Test
    void registerServices_registers_account_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(AccountRepository.class));
        assertEquals(SqliteAccountRepository.class, builder.singletonImplementations.get(AccountRepository.class));
    }

    @Test
    void registerServices_registers_balance_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(BalanceRepository.class));
        assertEquals(SqliteBalanceRepository.class, builder.singletonImplementations.get(BalanceRepository.class));
    }

    @Test
    void registerServices_registers_storage_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(SqlConnectionFactory.class));
    }

    @Test
    void registerServices_registers_config_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(FeatureManager.class));
        assertTrue(builder.singletonImplementations.containsKey(VaultLegacyAdapterConfig.class));
        assertEquals(DefaultVaultLegacyAdapterConfig.class,
            builder.singletonImplementations.get(VaultLegacyAdapterConfig.class));
        assertTrue(builder.singletonImplementations.containsKey(EconomyConfig.class));
        assertEquals(YamlEconomyConfig.class, builder.singletonImplementations.get(EconomyConfig.class));
        assertTrue(builder.singletonImplementations.containsKey(NumberFormatter.class));
        assertTrue(builder.singletonImplementations.containsKey(CurrencyFormatter.class));
        assertEquals(DefaultNumberFormatter.class, builder.singletonImplementations.get(NumberFormatter.class));
        assertEquals(DefaultCurrencyFormatter.class, builder.singletonImplementations.get(CurrencyFormatter.class));
    }

    private static final class NoopBuilder implements JConomyServiceRegistrar {
        private final Map<Class<?>, Class<?>> singletonImplementations = new HashMap<>();

        @Override
        public <T> JConomyServiceRegistrar addSingleton(Class<T> type) {
            return this;
        }

        @Override
        public <S, T extends S> JConomyServiceRegistrar addSingleton(Class<S> type, Class<T> implementationType) {
            singletonImplementations.put(type, implementationType);
            return this;
        }

        @Override
        public <T> JConomyServiceRegistrar addSingleton(Class<T> type, T instance) {
            return this;
        }

        @Override
        public <T> JConomyServiceRegistrar addSingletonFactory(
                Class<T> type,
                Function<JConomyServiceProvider, T> factory) {
            return this;
        }

    }
}
