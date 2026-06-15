package org.jconomy.config;

public interface CacheConfig {
    int getLruLimit();

    PeriodicFlushConfig getPeriodicFlushConfig();

    public interface PeriodicFlushConfig {
        boolean isEnabled();
        int getIntervalTicks();
    }
}
