package org.jconomy.dependencyinjection;

import java.util.List;

/**
 * Provides access to services registered via {@link JConomyServiceBuilder}.
 * <p>
 * Allows resolving single or multiple services by type. Supports optional and required lookups.
 * </p>
 */
public interface JConomyServiceProvider {

    /**
     * Returns a registered service instance of the specified type.
     * <p>
     * If no service is registered for the given type, {@code null} is returned.
     * </p>
     *
     * @param type the class of the service to retrieve
     * @param <T>  the type of the service
     * @return the registered service instance, or {@code null} if not found
     */
    <T> T getService(Class<T> type);

    /**
     * Returns a registered service instance of the specified type.
     * <p>
     * If no service is registered for the given type, an exception is thrown.
     * </p>
     *
     * @param type the class of the service to retrieve
     * @param <T>  the type of the service
     * @return the registered service instance
     * @throws RuntimeException if no service is found for the given type
     */
    <T> T getRequiredService(Class<T> type);

    /**
     * Returns all registered service instances of the specified type.
     * <p>
     * The services are returned in the order they were registered. If no services are
     * registered for the type, an empty list is returned.
     * </p>
     *
     * @param type the class of the services to retrieve
     * @param <T>  the type of the services
     * @return a list of registered service instances, never {@code null}
     */
    <T> List<T> getServices(Class<T> type);
}
