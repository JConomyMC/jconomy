package org.jconomy.lifecycle;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.jconomy.extensions.ExtensionManager;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.junit.jupiter.api.Test;

class PluginShutdownTests {

    private final ExtensionManager extensionManager = mock(ExtensionManager.class);
    private final PeriodicFlushScheduler scheduler = mock(PeriodicFlushScheduler.class);
    private final FlushRegistry flushRegistry = mock(FlushRegistry.class);
    private final PluginShutdown shutdown = new PluginShutdown(extensionManager);

    @Test
    void run_with_nothing_initialized_closes_extensions_without_throwing() {
        shutdown.run(null, null);

        verify(extensionManager).close();
    }

    @Test
    void run_with_everything_initialized_stops_scheduler_then_flushes_then_closes_extensions() {
        shutdown.run(scheduler, flushRegistry);

        var order = inOrder(scheduler, flushRegistry, extensionManager);
        order.verify(scheduler).stop();
        order.verify(flushRegistry).flushAll();
        order.verify(extensionManager).close();
    }

    @Test
    void run_without_scheduler_flushes_then_closes_extensions() {
        shutdown.run(null, flushRegistry);

        var order = inOrder(flushRegistry, extensionManager);
        order.verify(flushRegistry).flushAll();
        order.verify(extensionManager).close();
    }
}
