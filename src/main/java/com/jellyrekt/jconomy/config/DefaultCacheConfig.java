package com.jellyrekt.jconomy.config;

import com.jellyrekt.jconomy.storage.ConfigFileConfigurationProvider;
import com.jellyrekt.storage.fileconfiguration.FileConfigurationStorage;

public class DefaultCacheConfig extends FileConfigurationStorage implements CacheConfig {
    private final String baseKey = "cache";

    public DefaultCacheConfig(ConfigFileConfigurationProvider configurationProvider) {
        super(configurationProvider);
        //TODO Auto-generated constructor stub
    }

    @Override
    public int getLruLimit() {
        return getInt("lru-limit", 10000);
    }
    
    private int getInt(String key, int defaultValue) {
        return getFileConfiguration().getInt(getFullKey(key), defaultValue);
    }

    private String getFullKey(String partialKey) {
        return baseKey == null || baseKey == ""
            ? partialKey
            : baseKey;
    }
        
}
