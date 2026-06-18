package org.jconomy.impl.bootstrap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.DefaultAccountAccess;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.accounts.DefaultBalanceAccess;
import org.jconomy.accounts.AccountCache;
import org.jconomy.accounts.LruAccountCache;
import org.jconomy.accounts.BalanceCache;
import org.jconomy.accounts.LruBalanceCache;
import org.jconomy.accounts.AccountRepository;
import org.jconomy.accounts.SqliteAccountRepository;
import org.jconomy.accounts.BalanceRepository;
import org.jconomy.accounts.SqliteBalanceRepository;

class JConomyImplRegistrarTests {

    @Test
    void registerServices_accepts_builder() {
        var builder = new NoopBuilder();

        assertDoesNotThrow(() -> JConomyImplRegistrar.registerServices(builder));
    }

    @Test
    void registerServices_registers_default_flush_registry() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(FlushRegistry.class));
        assertEquals(DefaultFlushRegistry.class, builder.singletonImplementations.get(FlushRegistry.class));
    }

    @Test
    void registerServices_registers_account_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(AccountCache.class));
        assertEquals(LruAccountCache.class, builder.singletonImplementations.get(AccountCache.class));
        assertTrue(builder.singletonImplementations.containsKey(AccountRepository.class));
        assertEquals(SqliteAccountRepository.class, builder.singletonImplementations.get(AccountRepository.class));
        assertTrue(builder.singletonImplementations.containsKey(AccountAccess.class));
        assertEquals(DefaultAccountAccess.class, builder.singletonImplementations.get(AccountAccess.class));
    }

    @Test
    void registerServices_registers_balance_implementations() {
        var builder = new NoopBuilder();

        JConomyImplRegistrar.registerServices(builder);

        assertTrue(builder.singletonImplementations.containsKey(BalanceCache.class));
        assertEquals(LruBalanceCache.class, builder.singletonImplementations.get(BalanceCache.class));
        assertTrue(builder.singletonImplementations.containsKey(BalanceRepository.class));
        assertEquals(SqliteBalanceRepository.class, builder.singletonImplementations.get(BalanceRepository.class));
        assertTrue(builder.singletonImplementations.containsKey(BalanceAccess.class));
        assertEquals(DefaultBalanceAccess.class, builder.singletonImplementations.get(BalanceAccess.class));
    }

    private static final class NoopBuilder implements JConomyServiceBuilder {
        private final Map<Class<?>, Class<?>> singletonImplementations = new HashMap<>();

        @Override
        public <T> JConomyServiceBuilder addSingleton(Class<T> type) {
            return this;
        }

        @Override
        public <S, T extends S> JConomyServiceBuilder addSingleton(Class<S> type, Class<T> implementationType) {
            singletonImplementations.put(type, implementationType);
            return this;
        }

        @Override
        public <T> JConomyServiceBuilder addSingleton(Class<T> type, T instance) {
            return this;
        }

        @Override
        public <T> JConomyServiceBuilder addSingletonFactory(
                Class<T> type,
                Function<JConomyServiceProvider, T> factory) {
            return this;
        }

        @Override
        public JConomyServiceProvider build() {
            return new JConomyServiceProvider() {
                @Override
                public <T> T getService(Class<T> type) {
                    return null;
                }

                @Override
                public <T> T getRequiredService(Class<T> type) {
                    throw new UnsupportedOperationException("Not needed for this test");
                }

                @Override
                public <T> List<T> getServices(Class<T> type) {
                    return List.of();
                }
            };
        }
    }
}
