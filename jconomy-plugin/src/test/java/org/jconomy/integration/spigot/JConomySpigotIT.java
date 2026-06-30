package org.jconomy.integration.spigot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "jconomy.integration.spigot", matches = "true")
class JConomySpigotIT {

    @Test
    void spigotIntegrationHarnessIsActivatedBySystemProperty() {
        assertTrue(Boolean.getBoolean("jconomy.integration.spigot"));
    }
}
