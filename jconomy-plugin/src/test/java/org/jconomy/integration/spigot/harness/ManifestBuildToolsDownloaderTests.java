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
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManifestBuildToolsDownloaderTests {

    @TempDir
    Path tempDir;

    @Test
    void downloadFetchesBuildToolsFromManifestUrlAndVerifiesChecksum() {
        RecordingArtifactFetcher fetcher = new RecordingArtifactFetcher("buildtools");
        ArtifactLockManifest manifest = manifest(sha256("buildtools"));
        ManifestBuildToolsDownloader downloader = new ManifestBuildToolsDownloader(manifest, fetcher);

        Path destination = tempDir.resolve("BuildTools.jar");
        downloader.download(destination);

        assertEquals(
                "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar",
                fetcher.lastUrl
        );
        assertEquals(destination, fetcher.lastDestination);
    }

    @Test
    void downloadFailsWhenDownloadedFileChecksumDoesNotMatchManifest() {
        RecordingArtifactFetcher fetcher = new RecordingArtifactFetcher("unexpected-content");
        ArtifactLockManifest manifest = manifest(sha256("buildtools"));
        ManifestBuildToolsDownloader downloader = new ManifestBuildToolsDownloader(manifest, fetcher);

        Path destination = tempDir.resolve("BuildTools.jar");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> downloader.download(destination)
        );

        assertEquals("SHA-256 mismatch for " + destination, error.getMessage());
    }

    private static ArtifactLockManifest manifest(String buildToolsSha256) {
        return new ArtifactLockManifest(
                new ArtifactLockManifest.BuildToolsArtifact(
                        "latest",
                        "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar",
                        buildToolsSha256
                ),
                new ArtifactLockManifest.SpigotArtifact("1.21.8", "1.21.8"),
                List.of()
        );
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
        private Path lastDestination;

        private RecordingArtifactFetcher(String content) {
            this.content = content;
        }

        @Override
        public void download(String url, Path destination) {
            this.lastUrl = url;
            this.lastDestination = destination;

            try {
                Files.createDirectories(destination.getParent());
                Files.writeString(destination, content);
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }
}
