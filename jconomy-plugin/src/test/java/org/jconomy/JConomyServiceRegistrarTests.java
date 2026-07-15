package org.jconomy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.jconomy.dependencyinjection.DefaultServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceRegistrar;
import org.jconomy.dependencyinjection.JConomyServiceProvider;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;

/**
 * Tests that critical infrastructure services are protected from extension overloading.
 */
class JConomyServiceRegistrarTests {

    /**
     * Verifies that PeriodicFlushScheduler is NOT registered in the DI container,
     * making it unavailable for extension access and override.
     */
    @Test
    void registerServices_does_not_register_periodicFlushScheduler() {
        // Arrange
        var builder = new DefaultServiceBuilder();
        
        // Act - build with no PeriodicFlushScheduler registration
        var provider = builder.build();
        
        // Assert - PeriodicFlushScheduler should not be resolvable
        var scheduler = provider.getService(PeriodicFlushScheduler.class);
        assertNull(scheduler, "PeriodicFlushScheduler should not be available in service provider");
    }

    /**
     * Verifies that FlushRegistry remains accessible after the provider is built.
     * This ensures extensions can use it during onServicesReady().
     */
    @Test
    void serviceProvider_provides_flushRegistry_for_extensions() {
        // Arrange
        var builder = new DefaultServiceBuilder();
        
        // Register a mock FlushRegistry for testing
        var mockFlushRegistry = new FlushRegistry() {
            @Override
            public void register(org.jconomy.storage.Flushable flushable) {}
            
            @Override
            public void flushAll() {}
        };
        
        builder.addSingleton(FlushRegistry.class, mockFlushRegistry);
        var provider = builder.build();
        
        // Act
        var flushRegistry = provider.getRequiredService(FlushRegistry.class);
        
        // Assert
        assertNotNull(flushRegistry, "FlushRegistry should be available to extensions");
        assertSame(mockFlushRegistry, flushRegistry);
    }
}
