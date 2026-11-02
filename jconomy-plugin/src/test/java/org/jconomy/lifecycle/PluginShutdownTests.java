package org.jconomy.lifecycle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.logging.Logger;

import org.jconomy.extensions.ExtensionManager;
import org.junit.jupiter.api.Test;

class PluginShutdownTests {

    private final ExtensionManager extensionManager = mock(ExtensionManager.class);
    private final PluginShutdown shutdown = new PluginShutdown(extensionManager, Logger.getAnonymousLogger());

    @Test
    void run_with_nothing_initialized_closes_extensions_without_throwing() {
        shutdown.run(null, null);

        verify(extensionManager).close();
    }
}
