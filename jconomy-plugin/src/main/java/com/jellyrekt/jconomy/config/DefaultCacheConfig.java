package com.jellyrekt.jconomy.config;

public class DefaultCacheConfig implements CacheConfig {
    private final JConomyConfig config;

    public DefaultCacheConfig(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public int getLruLimit() {
        return config.getSection("cache").getInt("lru-limit", 10000);
    }
}
