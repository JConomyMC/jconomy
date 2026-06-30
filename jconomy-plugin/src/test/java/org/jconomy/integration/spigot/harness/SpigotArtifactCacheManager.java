package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

class SpigotArtifactCacheManager {

    private final Path cacheRoot;
    private final String spigotVersion;
    private final BuildToolsDownloader buildToolsDownloader;
    private final SpigotJarBuilder spigotJarBuilder;

    SpigotArtifactCacheManager(
            Path cacheRoot,
            String spigotVersion,
            BuildToolsDownloader buildToolsDownloader,
            SpigotJarBuilder spigotJarBuilder
    ) {
        this.cacheRoot = Objects.requireNonNull(cacheRoot, "cacheRoot");
        this.spigotVersion = Objects.requireNonNull(spigotVersion, "spigotVersion");
        this.buildToolsDownloader = Objects.requireNonNull(buildToolsDownloader, "buildToolsDownloader");
        this.spigotJarBuilder = Objects.requireNonNull(spigotJarBuilder, "spigotJarBuilder");
    }

    Path ensureSpigotJar() {
        Path buildToolsJar = cacheRoot.resolve("buildtools/BuildTools.jar");
        Path cachedSpigotJar = cacheRoot.resolve("servers/spigot").resolve(spigotVersion).resolve("spigot.jar");

        if (!Files.exists(buildToolsJar)) {
            createDirectories(buildToolsJar.getParent());
            buildToolsDownloader.download(buildToolsJar);
            assertExists(buildToolsJar, "BuildTools jar was not downloaded.");
        }

        if (Files.exists(cachedSpigotJar)) {
            return cachedSpigotJar;
        }

        createDirectories(cachedSpigotJar.getParent());
        Path workspace = createTempWorkspace();

        try {
            Path builtSpigotJar = spigotJarBuilder.buildSpigotJar(buildToolsJar, spigotVersion, workspace);
            assertExists(builtSpigotJar, "BuildTools did not produce a Spigot jar.");
            Files.copy(builtSpigotJar, cachedSpigotJar);
            return cachedSpigotJar;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to cache Spigot jar.", exception);
        } finally {
            deleteRecursively(workspace);
        }
    }

    private Path createTempWorkspace() {
        try {
            Path workspacesRoot = cacheRoot.resolve("tmp");
            createDirectories(workspacesRoot);
            return Files.createTempDirectory(workspacesRoot, "buildtools-");
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create BuildTools workspace.", exception);
        }
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create directory: " + directory, exception);
        }
    }

    private void assertExists(Path file, String message) {
        if (!Files.exists(file)) {
            throw new IllegalStateException(message + " Missing file: " + file);
        }
    }

    private void deleteRecursively(Path root) {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to delete temporary path: " + path, exception);
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to clean temporary BuildTools workspace.", exception);
        }
    }
}
