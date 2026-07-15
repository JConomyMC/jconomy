package org.jconomy.dependencyinjection;

import java.util.function.Function;

import com.merenze.dependencyinjection.ServiceBuilder;

public class DefaultServiceBuilder implements JConomyServiceRegistrar {
    private final ServiceBuilder internalBuilder = new ServiceBuilder();
    private JConomyServiceProvider provider;
    private boolean isBuilt = false;

    protected void validateState() {
        if (isBuilt) {
            throw new IllegalStateException("Cannot register services after the provider is built.");
        }
    }

    @Override
    public <T> JConomyServiceRegistrar addSingleton(Class<T> type) {
        validateState();
        internalBuilder.addSingleton(type);
        return this;
    }

    @Override
    public <S, T extends S> JConomyServiceRegistrar addSingleton(Class<S> type, Class<T> implementationType) {
        validateState();
        internalBuilder.addSingleton(type, implementationType);
        return this;
    }

    @Override
    public <T> JConomyServiceRegistrar addSingleton(Class<T> type, T instance) {
        validateState();
        internalBuilder.addSingleton(type, instance);
        return this;
    }

    @Override
    public <T> JConomyServiceRegistrar addSingletonFactory(Class<T> type, Function<JConomyServiceProvider, T> factory) {
        validateState();
        internalBuilder.addSingletonFactory(type, internalProvider -> {
            var provider = internalProvider.getRequiredService(JConomyServiceProvider.class);
            return factory.apply(provider);
        });
        return this;
    }

    public JConomyServiceProvider build() {
        if (isBuilt) {
            throw new IllegalStateException("Provider has already been built");
        }
        var provider = new DefaultServiceProvider();
        internalBuilder.addSingleton(JConomyServiceProvider.class, provider);
        var internalProvider = internalBuilder.build(true);
        provider.setDelegate(internalProvider);
        isBuilt = true;
        return provider;
    }
    
}
