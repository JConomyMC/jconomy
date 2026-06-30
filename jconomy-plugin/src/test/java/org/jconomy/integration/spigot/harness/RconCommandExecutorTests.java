package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RconCommandExecutorTests {

    @Test
    void executeReturnsFailureWhenConnectionCannotBeEstablished() {
        RconCommandExecutor executor = new RconCommandExecutor("127.0.0.1", 1, "test-password", Duration.ofMillis(100));

        CommandResult result = executor.execute("version");

        assertFalse(result.success());
    }

    @Test
    void executeIncludesFailureOutputWhenConnectionFails() {
        RconCommandExecutor executor = new RconCommandExecutor("127.0.0.1", 1, "test-password", Duration.ofMillis(100));

        CommandResult result = executor.execute("plugins");

        assertTrue(result.output() != null && !result.output().isBlank());
    }
}
