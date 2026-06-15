package org.jconomy.config;

public class DefaultCacheConfig implements CacheConfig {
    private final JConomyConfig config;
    private final PeriodicFlushConfig periodicFlushConfig;

    public DefaultCacheConfig(JConomyConfig config) {
        this.config = config;
        periodicFlushConfig = new DefaultPeriodicFlushConfig(config);
    }

    @Override
    public PeriodicFlushConfig getPeriodicFlushConfig() {
        return new DefaultPeriodicFlushConfig(config);
    }

    @Override
    public int getLruLimit() {
        return config.getSection("cache").getInt("lru-limit", 10000);
    }

    public class DefaultPeriodicFlushConfig implements PeriodicFlushConfig {
        private static final String SECTION = "cache.periodic-flush.";

        private final JConomyConfig config;

        public DefaultPeriodicFlushConfig(JConomyConfig config) {
            this.config = config;
        }

        @Override
        public boolean isEnabled() {
            return config.getBoolean(SECTION + "enabled", true);
        }

        @Override
        public int getIntervalTicks() {
            return config.getInt(SECTION + "interval-ticks", 1200);
        }
    }
}
