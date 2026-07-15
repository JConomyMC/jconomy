package org.jconomy.dependencyinjection;

import java.util.function.Function;

/**
 * Builder interface for registering services in the JConomy dependency injection system.
 * <p>
 * Allows registration of singleton instances, implementation types, and factories, 
 * and produces a {@link JConomyServiceProvider} for resolving the services.
 * </p>
 */
public interface JConomyServiceBuilder {

    /**
     * Registers a singleton service by its type. The implementation will be the type itself.
     * The instance is created lazily when first requested.
     *
     * @param type the class of the service to register
     * @param <T>  the type of the service
     * @return this builder for chaining
     */
    <T> JConomyServiceBuilder addSingleton(Class<T> type);

    /**
     * Registers a singleton service by an abstraction and a concrete implementation type.
     * The instance of {@code implementationType} is created lazily when first requested.
     *
     * @param type               the service interface or base class
     * @param implementationType the concrete implementation class
     * @param <S>                the abstraction type
     * @param <T>                the concrete implementation type
     * @return this builder for chaining
     */
    <S, T extends S> JConomyServiceBuilder addSingleton(Class<S> type, Class<T> implementationType);

    /**
     * Registers a singleton service with a pre-created instance.
     * The provided instance will always be returned for this service type.
     *
     * @param type     the class of the service
     * @param instance the pre-created singleton instance
     * @param <T>      the type of the service
     * @return this builder for chaining
     */
    <T> JConomyServiceBuilder addSingleton(Class<T> type, T instance);

    /**
     * Registers a singleton service using a factory function.
     * The factory is invoked once to create the singleton instance when first requested.
     *
     * @param type    the class of the service
     * @param factory a function that accepts a {@link JConomyServiceProvider} and returns the service instance
     * @param <T>     the type of the service
     * @return this builder for chaining
     */
    <T> JConomyServiceBuilder addSingletonFactory(Class<T> type, Function<JConomyServiceProvider, T> factory);
}
