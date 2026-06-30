package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotArtifactCacheManagerManifestWiringTests {

    @TempDir
    Path tempDir;

    @Test
    void fromManifestUsesPinnedVersionUrlAndChecksum() {
        RecordingArtifactFetcher fetcher = new RecordingArtifactFetcher("buildtools");
        RecordingSpigotJarBuilder builder = new RecordingSpigotJarBuilder();

        ArtifactLockManifest manifest = new ArtifactLockManifest(
                new ArtifactLockManifest.BuildToolsArtifact(
                        "latest",
                        "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar",
                        sha256("buildtools")
                ),
                new ArtifactLockManifest.SpigotArtifact("1.21.8", "1.21.8"),
                List.of()
        );

        SpigotArtifactCacheManager manager = SpigotArtifactCacheManager.fromManifest(
                tempDir,
                manifest,
                fetcher,
                builder
        );

        Path spigotJar = manager.ensureSpigotJar();

        assertEquals(
                "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar",
                fetcher.lastUrl
        );
        assertTrue(Files.exists(spigotJar));
        assertEquals(tempDir.resolve("servers/spigot/1.21.8/spigot.jar"), spigotJar);
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

    private static final class RecordingArtifactFetcher implements ArtifactFetcher {

        private final String content;
        private String lastUrl;

        private RecordingArtifactFetcher(String content) {
            this.content = content;
        }

        @Override
        public void download(String url, Path destination) {
            this.lastUrl = url;
            try {
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, content);
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    private static final class RecordingSpigotJarBuilder implements SpigotJarBuilder {

        @Override
        public Path buildSpigotJar(Path buildToolsJar, String spigotVersion, Path workspace) {
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
}
