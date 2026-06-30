package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotArtifactCacheManagerTests {

    @TempDir
    Path tempDir;

    @Test
    void ensureSpigotJarDownloadsBuildToolsWhenMissing() {
        RecordingBuildToolsDownloader downloader = new RecordingBuildToolsDownloader();
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();
        SpigotArtifactCacheManager manager = new SpigotArtifactCacheManager(
                tempDir,
                "1.21.8",
            sha256("buildtools"),
                downloader,
                builder
        );

        Path spigotJar = manager.ensureSpigotJar();

        assertEquals(1, downloader.downloadCalls);
        assertTrue(Files.exists(downloader.lastDestination));
        assertTrue(Files.exists(spigotJar));
    }

    @Test
    void ensureSpigotJarSkipsBuildToolsDownloadWhenCached() throws IOException {
        Path buildToolsJar = tempDir.resolve("buildtools/BuildTools.jar");
        Files.createDirectories(buildToolsJar.getParent());
        Files.writeString(buildToolsJar, "buildtools");

        RecordingBuildToolsDownloader downloader = new RecordingBuildToolsDownloader();
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();
        SpigotArtifactCacheManager manager = new SpigotArtifactCacheManager(
                tempDir,
                "1.21.8",
            sha256("buildtools"),
                downloader,
                builder
        );

        manager.ensureSpigotJar();

        assertEquals(0, downloader.downloadCalls);
    }

    @Test
    void ensureSpigotJarBuildsAndCachesWhenMissing() {
        RecordingBuildToolsDownloader downloader = new RecordingBuildToolsDownloader();
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();
        SpigotArtifactCacheManager manager = new SpigotArtifactCacheManager(
                tempDir,
                "1.21.8",
            sha256("buildtools"),
                downloader,
                builder
        );

        Path spigotJar = manager.ensureSpigotJar();

        assertEquals(1, builder.buildCalls);
        assertNotNull(builder.lastWorkspace);
        assertFalse(Files.exists(builder.lastWorkspace));
        assertEquals(tempDir.resolve("servers/spigot/1.21.8/spigot.jar"), spigotJar);
        assertTrue(Files.exists(spigotJar));
    }

    @Test
    void ensureSpigotJarSkipsBuildWhenCached() throws IOException {
        Path cachedSpigotJar = tempDir.resolve("servers/spigot/1.21.8/spigot.jar");
        Files.createDirectories(cachedSpigotJar.getParent());
        Files.writeString(cachedSpigotJar, "cached-spigot");

        RecordingBuildToolsDownloader downloader = new RecordingBuildToolsDownloader();
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();
        SpigotArtifactCacheManager manager = new SpigotArtifactCacheManager(
                tempDir,
                "1.21.8",
            sha256("buildtools"),
                downloader,
                builder
        );

        Path resolvedSpigotJar = manager.ensureSpigotJar();

        assertEquals(0, builder.buildCalls);
        assertEquals(cachedSpigotJar, resolvedSpigotJar);
    }

    @Test
    void ensureSpigotJarRedownloadsBuildToolsWhenCachedChecksumIsInvalid() throws IOException {
        Path buildToolsJar = tempDir.resolve("buildtools/BuildTools.jar");
        Files.createDirectories(buildToolsJar.getParent());
        Files.writeString(buildToolsJar, "corrupt-buildtools");

        RecordingBuildToolsDownloader downloader = new RecordingBuildToolsDownloader();
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();
        SpigotArtifactCacheManager manager = new SpigotArtifactCacheManager(
                tempDir,
                "1.21.8",
                sha256("buildtools"),
                downloader,
                builder
        );

        manager.ensureSpigotJar();

        assertEquals(1, downloader.downloadCalls);
        assertEquals("buildtools", Files.readString(buildToolsJar));
    }

    private static final class RecordingBuildToolsDownloader implements BuildToolsDownloader {

        private int downloadCalls;
        private Path lastDestination;

        @Override
        public void download(Path destination) {
            this.downloadCalls++;
            this.lastDestination = destination;
            try {
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, "buildtools");
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    private static final class RecordingSpigotJarBuilder implements SpigotJarBuilder {

        private int buildCalls;
        private Path lastWorkspace;

        @Override
        public Path buildSpigotJar(Path buildToolsJar, String spigotVersion, Path workspace) {
            this.buildCalls++;
            this.lastWorkspace = workspace;

            Path builtJar = workspace.resolve("spigot-generated.jar");
            try {
                Files.createDirectories(workspace);
                Files.writeString(builtJar, "spigot-jar-" + spigotVersion + "-from-" + buildToolsJar.getFileName());
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }

            return builtJar;
        }
    }

    private static String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                hex.append(String.format("%02x", value));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
