package org.jconomy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class DefaultPeriodicFlushConfigTests {

    private static CacheConfig.PeriodicFlushConfig configWith(JConomyConfig root) {
        return new DefaultCacheConfig(root).getPeriodicFlushConfig();
    }

    @Test
    void isEnabled_reads_enabled_from_config() {
        var root = mock(JConomyConfig.class);
        when(root.getBoolean("cache.periodic-flush.enabled", true)).thenReturn(false);

        assertFalse(configWith(root).isEnabled());
    }

    @Test
    void isEnabled_returns_true_by_default() {
        var root = mock(JConomyConfig.class);
        when(root.getBoolean("cache.periodic-flush.enabled", true)).thenReturn(true);

        assertTrue(configWith(root).isEnabled());
    }

    @Test
    void getIntervalTicks_reads_interval_ticks_from_config() {
        var root = mock(JConomyConfig.class);
        when(root.getInt("cache.periodic-flush.interval-ticks", 1200)).thenReturn(600);

        assertEquals(600, configWith(root).getIntervalTicks());
    }

    @Test
    void getIntervalTicks_returns_default_when_key_absent() {
        var root = mock(JConomyConfig.class);
        when(root.getInt("cache.periodic-flush.interval-ticks", 1200)).thenReturn(1200);

        assertEquals(1200, configWith(root).getIntervalTicks());
    }
}
