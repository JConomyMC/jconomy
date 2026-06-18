package org.jconomy.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFlushRegistryTests {

    private DefaultFlushRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultFlushRegistry();
    }

    @Test
    void flushAll_calls_flush_on_registered_flushable() {
        var flushed = new boolean[] { false };
        registry.register(() -> flushed[0] = true);

        registry.flushAll();

        assertTrue(flushed[0]);
    }

    @Test
    void flushAll_calls_flush_on_all_registered_flushables() {
        var flushCount = new int[] { 0 };
        registry.register(() -> flushCount[0]++);
        registry.register(() -> flushCount[0]++);
        registry.register(() -> flushCount[0]++);

        registry.flushAll();

        assertEquals(3, flushCount[0]);
    }

    @Test
    void flushAll_does_nothing_when_no_flushables_registered() {
        assertDoesNotThrow(() -> registry.flushAll());
    }
}
