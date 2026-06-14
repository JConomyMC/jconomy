package com.jellyrekt.jconomy;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;

public interface JConomyExpansion {
    String getName();

    /**
     * Called once during plugin startup to allow the expansion to register services
     * with the shared dependency injection container.
     * <p>
     * This method is invoked before the {@link com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider}
     * is built. The {@code builder} must not be used after this method returns.
     * </p>
     */
    void configureServices(JConomyServiceBuilder builder);

    /**
     * Called after all expansions have finished configuring services and the
     * {@link com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider} is fully built.
     * <p>
     * This is the first point at which it is safe to resolve services from the provider.
     * {@code configureServices} is guaranteed to have been called on every loaded expansion
     * before this method is invoked on any of them.
     * </p>
     */
    void onServicesReady(JConomyServiceProvider provider);
}
