package org.jconomy;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public interface JConomyExtension {
    String getName();

    /**
     * Called once during plugin startup to allow the extension to register services
     * with the shared dependency injection container.
     * <p>
     * This method is invoked before the {@link org.jconomy.dependencyinjection.JConomyServiceProvider}
     * is built. The {@code builder} must not be used after this method returns.
     * </p>
     */
    default void configureServices(JConomyServiceBuilder builder) {}

    /**
     * Called after all extensions have finished configuring services and the
     * {@link org.jconomy.dependencyinjection.JConomyServiceProvider} is fully built.
     * <p>
     * This is the first point at which it is safe to resolve services from the provider.
     * {@code configureServices} is guaranteed to have been called on every loaded extension
     * before this method is invoked on any of them.
     * </p>
     */
    default void onServicesReady(JConomyServiceProvider provider) {}
}
