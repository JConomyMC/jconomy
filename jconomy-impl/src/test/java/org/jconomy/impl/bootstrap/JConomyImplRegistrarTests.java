package org.jconomy.impl.bootstrap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

class JConomyImplRegistrarTests {

    @Test
    void registerServices_accepts_builder() {
        var builder = new NoopBuilder();

        assertDoesNotThrow(() -> JConomyImplRegistrar.registerServices(builder));
    }

    private static final class NoopBuilder implements JConomyServiceBuilder {

        @Override
        public <T> JConomyServiceBuilder addSingleton(Class<T> type) {
            return this;
        }

        @Override
        public <S, T extends S> JConomyServiceBuilder addSingleton(Class<S> type, Class<T> implementationType) {
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
