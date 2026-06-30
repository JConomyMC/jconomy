package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotIntegrationSettingsTests {

    @Test
    void resolveUsesSystemPropertyCachePathBeforeEnvironment() {
        SpigotIntegrationSettings settings = SpigotIntegrationSettings.resolve(
                Map.of("jellyrekt.test.cache", "/tmp/property-cache"),
                Map.of("JELLYREKT_TEST_CACHE", "/tmp/env-cache"),
                Path.of("/home/test-user")
        );

        assertEquals(Path.of("/tmp/property-cache"), settings.cacheRoot());
    }

    @Test
    void resolveUsesEnvironmentCachePathWhenSystemPropertyMissing() {
        SpigotIntegrationSettings settings = SpigotIntegrationSettings.resolve(
                Map.of(),
                Map.of("JELLYREKT_TEST_CACHE", "/tmp/env-cache"),
                Path.of("/home/test-user")
        );

        assertEquals(Path.of("/tmp/env-cache"), settings.cacheRoot());
    }

    @Test
    void resolveUsesDefaultCachePathWhenNoOverridesArePresent() {
        SpigotIntegrationSettings settings = SpigotIntegrationSettings.resolve(
                Map.of(),
                Map.of(),
                Path.of("/home/test-user")
        );

        assertEquals(Path.of("/home/test-user/.jellyrekt/test-cache"), settings.cacheRoot());
    }

    @Test
    void resolveSupportsKeepRunsFlag() {
        SpigotIntegrationSettings enabled = SpigotIntegrationSettings.resolve(
                Map.of("jconomy.integration.keepRuns", "true"),
                Map.of(),
                Path.of("/home/test-user")
        );

        SpigotIntegrationSettings disabled = SpigotIntegrationSettings.resolve(
                Map.of(),
                Map.of(),
                Path.of("/home/test-user")
        );

        assertTrue(enabled.keepRuns());
        assertFalse(disabled.keepRuns());
    }

    @Test
    void resolveRejectsBlankCachePathSystemProperty() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> SpigotIntegrationSettings.resolve(
                        Map.of("jellyrekt.test.cache", "   "),
                        Map.of(),
                        Path.of("/home/test-user")
                )
        );

        assertEquals("jellyrekt.test.cache cannot be blank.", error.getMessage());
    }

    @Test
    void resolveUsesExpectedDefaultsForOtherCoreSettings() {
        SpigotIntegrationSettings settings = SpigotIntegrationSettings.resolve(
                Map.of(),
                Map.of(),
                Path.of("/home/test-user")
        );

        assertEquals(Path.of("target/integration-runs"), settings.runRoot());
        assertEquals("1.21.8", settings.spigotVersion());
        assertEquals("jellyrekt-minecraft-test-runtime:latest", settings.dockerImageName());
    }
}
