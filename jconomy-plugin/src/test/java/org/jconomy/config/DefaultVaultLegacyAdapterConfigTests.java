package org.jconomy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class DefaultVaultLegacyAdapterConfigTests {

    @Test
    void isEnabled_returns_true_when_config_value_is_true() {
        var section = mock(JConomyConfig.class);
        when(section.getBoolean("enabled", false)).thenReturn(true);

        var root = mock(JConomyConfig.class);
        when(root.getSection("vault-legacy-adapter")).thenReturn(section);

        var config = new DefaultVaultLegacyAdapterConfig(root);

        assertTrue(config.isEnabled());
    }

    @Test
    void isEnabled_returns_false_when_config_value_is_false() {
        var section = mock(JConomyConfig.class);
        when(section.getBoolean("enabled", false)).thenReturn(false);

        var root = mock(JConomyConfig.class);
        when(root.getSection("vault-legacy-adapter")).thenReturn(section);

        var config = new DefaultVaultLegacyAdapterConfig(root);

        assertFalse(config.isEnabled());
    }

    @Test
    void isEnabled_returns_false_when_key_absent() {
        var section = mock(JConomyConfig.class);
        when(section.getBoolean("enabled", false)).thenReturn(false);

        var root = mock(JConomyConfig.class);
        when(root.getSection("vault-legacy-adapter")).thenReturn(section);

        var config = new DefaultVaultLegacyAdapterConfig(root);

        assertFalse(config.isEnabled());
    }
}
