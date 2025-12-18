package com.jellyrekt.jconomy.expansions;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.jellyrekt.jconomy.JConomyExpansion;

public class DefaultExpansionManager implements ExpansionManager {
    private final Set<LoadedExpansion> loadedExpansions;
    private final Logger logger;

    public DefaultExpansionManager(ExpansionLoader loader, Logger logger) {
        loadedExpansions = loader.load();
        this.logger = logger;
    }

    @Override
    public Set<JConomyExpansion> getExpansions() {
        return loadedExpansions.stream().map(LoadedExpansion::expansion).collect(Collectors.toSet());
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
