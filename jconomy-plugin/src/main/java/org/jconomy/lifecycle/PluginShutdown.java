package org.jconomy.lifecycle;

import org.jconomy.extensions.ExtensionManager;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;

public class PluginShutdown {
    private final ExtensionManager extensionManager;

    public PluginShutdown(ExtensionManager extensionManager) {
        this.extensionManager = extensionManager;
    }

    public void run(PeriodicFlushScheduler scheduler, FlushRegistry flushRegistry) {
        if (scheduler != null) {
            scheduler.stop();
        }
        if (flushRegistry != null) {
            flushRegistry.flushAll();
        }
        extensionManager.close();
    }
}
