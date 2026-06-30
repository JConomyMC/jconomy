package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunWorkspaceCleanupTests {

    @TempDir
    Path tempDir;

    @Test
    void cleanupRemovesRunDirectoryOnSuccessWhenKeepRunsIsDisabled() throws Exception {
        Path runDir = tempDir.resolve("run-success");
        Files.createDirectories(runDir);
        Files.writeString(runDir.resolve("marker.txt"), "marker");

        RunWorkspaceCleanup.cleanup(runDir, false, true);

        assertFalse(Files.exists(runDir));
    }

    @Test
    void cleanupPreservesRunDirectoryOnFailure() throws Exception {
        Path runDir = tempDir.resolve("run-failure");
        Files.createDirectories(runDir);
        Files.writeString(runDir.resolve("marker.txt"), "marker");

        RunWorkspaceCleanup.cleanup(runDir, false, false);

        assertTrue(Files.exists(runDir));
    }

    @Test
    void cleanupPreservesRunDirectoryWhenKeepRunsEnabled() throws Exception {
        Path runDir = tempDir.resolve("run-keep");
        Files.createDirectories(runDir);
        Files.writeString(runDir.resolve("marker.txt"), "marker");

        RunWorkspaceCleanup.cleanup(runDir, true, true);

        assertTrue(Files.exists(runDir));
    }
}
