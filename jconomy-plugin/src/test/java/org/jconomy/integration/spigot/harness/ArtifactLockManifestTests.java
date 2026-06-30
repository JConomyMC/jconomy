package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtifactLockManifestTests {

    @Test
    void loadFromResourceReadsPinnedBuildToolsAndSpigotMetadata() {
        ArtifactLockManifest manifest = ArtifactLockManifestLoader.loadFromResource("integration/spigot/artifacts-lock.json");

        assertEquals("latest", manifest.buildTools().version());
        assertEquals(
                "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar",
                manifest.buildTools().url()
        );
        assertEquals("1.21.8", manifest.spigot().version());
        assertEquals("1.21.8", manifest.spigot().buildToolsRevision());
        assertEquals(1, manifest.plugins().size());
        assertEquals("EssentialsX", manifest.plugins().getFirst().name());
    }

    @Test
    void verifySha256ThrowsWhenChecksumDoesNotMatch() throws Exception {
        Path file = Files.createTempFile("checksum-mismatch", ".txt");
        Files.writeString(file, "abc", StandardCharsets.UTF_8);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> Sha256Verifier.verify(file, "deadbeef")
        );

        assertEquals("SHA-256 mismatch for " + file, error.getMessage());
    }

    @Test
    void verifySha256PassesWhenChecksumMatches() throws Exception {
        Path file = Files.createTempFile("checksum-match", ".txt");
        Files.writeString(file, "abc", StandardCharsets.UTF_8);

        Sha256Verifier.verify(file, "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
