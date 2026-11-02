package org.jconomy.lifecycle;

import java.util.logging.Logger;

import org.jconomy.extensions.ExtensionManager;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;

public class PluginShutdown {
    private final ExtensionManager extensionManager;
    private final Logger logger;

    public PluginShutdown(ExtensionManager extensionManager, Logger logger) {
        this.extensionManager = extensionManager;
        this.logger = logger;
    }

    public void run(PeriodicFlushScheduler scheduler, FlushRegistry flushRegistry) {
        extensionManager.close();
    }
}
