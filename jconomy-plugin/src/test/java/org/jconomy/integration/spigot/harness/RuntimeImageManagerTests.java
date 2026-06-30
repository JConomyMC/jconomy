package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeImageManagerTests {

    @Test
    void ensureImageBuildsWhenImageIsMissing() {
        RecordingDockerClient dockerClient = new RecordingDockerClient(false);
        RuntimeImageManager manager = new RuntimeImageManager(dockerClient);

        manager.ensureImage("jellyrekt-minecraft-test-runtime:latest", false);

        assertEquals(1, dockerClient.buildCalls);
        assertEquals("jellyrekt-minecraft-test-runtime:latest", dockerClient.lastBuildImage);
    }

    @Test
    void ensureImageSkipsBuildWhenImageAlreadyExists() {
        RecordingDockerClient dockerClient = new RecordingDockerClient(true);
        RuntimeImageManager manager = new RuntimeImageManager(dockerClient);

        manager.ensureImage("jellyrekt-minecraft-test-runtime:latest", false);

        assertEquals(0, dockerClient.buildCalls);
    }

    @Test
    void ensureImageBuildsWhenRefreshIsRequested() {
        RecordingDockerClient dockerClient = new RecordingDockerClient(true);
        RuntimeImageManager manager = new RuntimeImageManager(dockerClient);

        manager.ensureImage("jellyrekt-minecraft-test-runtime:latest", true);

        assertEquals(1, dockerClient.buildCalls);
    }

    private static final class RecordingDockerClient implements DockerClient {

        private final boolean imageExists;
        private int buildCalls;
        private String lastBuildImage;

        private RecordingDockerClient(boolean imageExists) {
            this.imageExists = imageExists;
        }

        @Override
        public boolean imageExists(String imageName) {
            return imageExists;
        }

        @Override
        public void buildImage(String imageName) {
            buildCalls++;
            lastBuildImage = imageName;
        }
    }
}
