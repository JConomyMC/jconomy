package com.jellyrekt.jconomy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class DefaultCacheConfigTests {

    @Test
    void getLruLimit_reads_from_cache_section() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getInt("lru-limit", 10000)).thenReturn(500);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertEquals(500, config.getLruLimit());
    }

    @Test
    void getLruLimit_returns_default_when_key_absent() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getInt("lru-limit", 10000)).thenReturn(10000);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertEquals(10000, config.getLruLimit());
    }
}
