package org.jconomy.expansions;

import java.util.Set;
import java.util.logging.Logger;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public class DefaultExpansionManager implements ExpansionManager {
    private final Set<LoadedExpansion> loadedExpansions;
    private final Logger logger;

    public DefaultExpansionManager(ExpansionLoader loader, Logger logger) {
        loadedExpansions = loader.load();
        this.logger = logger;
    }

    @Override
    public void configureServices(JConomyServiceBuilder builder) {
        loadedExpansions.forEach(expansion -> expansion.expansion().configureServices(builder));
    }

    @Override
    public void notifyServicesReady(JConomyServiceProvider provider) {
        loadedExpansions.forEach(expansion -> expansion.expansion().onServicesReady(provider));
    }

    @Override
    public void close() {
        loadedExpansions.forEach(expansion -> {
            try {
                expansion.classLoader().close();
            } catch (Exception ex) {
                logger.warning(String.format("Failed to close classloader for expansion '%s': %s", expansion.expansion().getName(),
                        ex.getMessage()));
            }
        });
    }
}
