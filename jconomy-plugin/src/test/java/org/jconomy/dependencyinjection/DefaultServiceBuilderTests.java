package org.jconomy.dependencyinjection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jconomy.storage.Flushable;

import org.junit.jupiter.api.Test;

class DefaultServiceBuilderTests {

    @Test
    void getServices_returns_all_registered_flushables_in_order() {
        var flushable1 = mock(Flushable.class);
        var flushable2 = mock(Flushable.class);
        var builder = new DefaultServiceBuilder();
        builder.addSingletonFactory(Flushable.class, sp -> flushable1);
        builder.addSingletonFactory(Flushable.class, sp -> flushable2);

        var provider = builder.build();
        var flushables = provider.getServices(Flushable.class);
        flushables.forEach(Flushable::flush);

        assertEquals(2, flushables.size());
        verify(flushable1).flush();
        verify(flushable2).flush();
    }

    @Test
    void factory_can_resolve_JConomyServiceProvider_from_within_lambda() {
        var builder = new DefaultServiceBuilder();
        builder.addSingletonFactory(String.class, serviceProvider -> {
            var self = serviceProvider.getRequiredService(JConomyServiceProvider.class);
            assertNotNull(self);
            return "resolved";
        });

        var provider = builder.build();
        var value = provider.getRequiredService(String.class);

        assertEquals("resolved", value);
    }

    @Test
    void build_twice_throws_IllegalStateException() {
        var builder = new DefaultServiceBuilder();
        builder.build();

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void addSingleton_after_build_throws_IllegalStateException() {
        var builder = new DefaultServiceBuilder();
        builder.build();

        assertThrows(IllegalStateException.class,
                () -> builder.addSingleton(String.class, "value"));
    }

    @Test
    void getRequiredService_before_setDelegate_throws_IllegalStateException() {
        var provider = new DefaultServiceProvider();

        assertThrows(IllegalStateException.class,
                () -> provider.getRequiredService(String.class));
    }
}
