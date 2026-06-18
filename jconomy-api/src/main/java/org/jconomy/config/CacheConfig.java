package org.jconomy.config;

public interface CacheConfig {
    int getLruLimit();

    boolean isWarmOnJoinEnabled();

    boolean isWarmOnTeleportEnabled();

    PeriodicFlushConfig getPeriodicFlushConfig();

    public interface PeriodicFlushConfig {
        boolean isEnabled();
        int getIntervalTicks();
    }
}
