package org.jconomy.integration.spigot.harness;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

final class ArtifactLockManifestLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

    private ArtifactLockManifestLoader() {
    }

    static ArtifactLockManifest loadFromResource(String resourcePath) {
        InputStream stream = ArtifactLockManifestLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalStateException("Manifest resource not found: " + resourcePath);
        }

        try (stream) {
            return OBJECT_MAPPER.readValue(stream, ArtifactLockManifest.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse manifest resource: " + resourcePath, exception);
        }
    }
}
