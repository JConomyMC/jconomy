package org.jconomy.config;

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

    @Test
    void isWarmOnJoinEnabled_reads_from_cache_section() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getBoolean("warm-on-join", true)).thenReturn(false);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertFalse(config.isWarmOnJoinEnabled());
    }

    @Test
    void isWarmOnJoinEnabled_returns_true_by_default() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getBoolean("warm-on-join", true)).thenReturn(true);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertTrue(config.isWarmOnJoinEnabled());
    }

    @Test
    void isWarmOnTeleportEnabled_reads_from_cache_section() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getBoolean("warm-on-teleport", true)).thenReturn(false);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertFalse(config.isWarmOnTeleportEnabled());
    }

    @Test
    void isWarmOnTeleportEnabled_returns_true_by_default() {
        var cacheSection = mock(JConomyConfig.class);
        when(cacheSection.getBoolean("warm-on-teleport", true)).thenReturn(true);

        var root = mock(JConomyConfig.class);
        when(root.getSection("cache")).thenReturn(cacheSection);

        var config = new DefaultCacheConfig(root);

        assertTrue(config.isWarmOnTeleportEnabled());
    }
}
