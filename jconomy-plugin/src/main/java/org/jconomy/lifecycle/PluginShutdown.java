package org.jconomy.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jconomy.extensions.ExtensionManager;
import org.jconomy.storage.FlushRegistry;
import org.jconomy.storage.PeriodicFlushScheduler;

public class PluginShutdown {
    private final PeriodicFlushScheduler scheduler;
    private final FlushRegistry flushRegistry;
    private final ExtensionManager extensionManager;
    private final Logger logger;

    public PluginShutdown(
            PeriodicFlushScheduler scheduler, FlushRegistry flushRegistry,
            ExtensionManager extensionManager, Logger logger) {
        this.scheduler = scheduler;
        this.flushRegistry = flushRegistry;
        this.extensionManager = extensionManager;
        this.logger = logger;
    }

    public void run() {
        if (scheduler != null) {
            runStep("Failed to stop periodic flush scheduler", scheduler::stop);
        }
        if (flushRegistry != null) {
            runStep("Failed to flush pending changes", flushRegistry::flushAll);
        }
        runStep("Failed to close extensions", extensionManager::close);
    }

    private void runStep(String failureMessage, Runnable step) {
        try {
            step.run();
        } catch (Exception ex) {
            logger.log(Level.WARNING, failureMessage, ex);
        }
    }
}
