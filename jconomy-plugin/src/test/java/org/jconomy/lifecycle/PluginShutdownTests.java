package org.jconomy.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jconomy.extensions.ExtensionManager;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PluginShutdownTests {

    private final ExtensionManager extensionManager = mock(ExtensionManager.class);
    private final PeriodicFlushScheduler scheduler = mock(PeriodicFlushScheduler.class);
    private final FlushRegistry flushRegistry = mock(FlushRegistry.class);
    private final Logger logger = Logger.getAnonymousLogger();
    private final CapturingLogHandler handler = new CapturingLogHandler();

    @BeforeEach
    void setUp() {
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    @Test
    void run_with_nothing_initialized_closes_extensions_without_throwing() {
        shutdown(null, null).run();

        verify(extensionManager).close();
    }

    @Test
    void run_with_everything_initialized_stops_scheduler_then_flushes_then_closes_extensions() {
        shutdown(scheduler, flushRegistry).run();

        var order = inOrder(scheduler, flushRegistry, extensionManager);
        order.verify(scheduler).stop();
        order.verify(flushRegistry).flushAll();
        order.verify(extensionManager).close();
    }

    @Test
    void run_without_scheduler_flushes_then_closes_extensions() {
        shutdown(null, flushRegistry).run();

        var order = inOrder(flushRegistry, extensionManager);
        order.verify(flushRegistry).flushAll();
        order.verify(extensionManager).close();
    }

    @Test
    void run_logs_scheduler_stop_failure_and_still_flushes_and_closes_extensions() {
        var failure = new IllegalStateException("simulated failure");
        doThrow(failure).when(scheduler).stop();

        shutdown(scheduler, flushRegistry).run();

        verify(flushRegistry).flushAll();
        verify(extensionManager).close();
        assertEquals(List.of(failure), handler.thrownWarnings());
    }

    @Test
    void run_logs_flush_failure_and_still_closes_extensions() {
        var failure = new IllegalStateException("simulated failure");
        doThrow(failure).when(flushRegistry).flushAll();

        shutdown(scheduler, flushRegistry).run();

        verify(extensionManager).close();
        assertEquals(List.of(failure), handler.thrownWarnings());
    }

    @Test
    void run_logs_extension_close_failure_without_throwing() {
        var failure = new IllegalStateException("simulated failure");
        doThrow(failure).when(extensionManager).close();

        shutdown(scheduler, flushRegistry).run();

        assertEquals(List.of(failure), handler.thrownWarnings());
    }

    private PluginShutdown shutdown(PeriodicFlushScheduler scheduler, FlushRegistry flushRegistry) {
        return new PluginShutdown(scheduler, flushRegistry, extensionManager, logger);
    }

    private static class CapturingLogHandler extends Handler {
        private final List<Throwable> thrownWarnings = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) thrownWarnings.add(record.getThrown());
        }

        @Override public void flush() {}
        @Override public void close() {}

        List<Throwable> thrownWarnings() {
            return thrownWarnings;
        }
    }
}
