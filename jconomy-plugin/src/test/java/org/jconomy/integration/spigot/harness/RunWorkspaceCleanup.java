package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

final class RunWorkspaceCleanup {

    private RunWorkspaceCleanup() {
    }

    static void cleanup(Path runDirectory, boolean keepRuns, boolean success) {
        if (keepRuns || !success) {
            return;
        }

        deleteRecursively(runDirectory);
    }

    private static void deleteRecursively(Path root) {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to delete run path: " + path, exception);
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to remove run directory: " + root, exception);
        }
    }
}
