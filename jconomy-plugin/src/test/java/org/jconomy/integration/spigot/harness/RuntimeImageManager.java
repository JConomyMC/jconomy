package org.jconomy.integration.spigot.harness;

import java.util.Objects;

class RuntimeImageManager {

    private final DockerClient dockerClient;

    RuntimeImageManager(DockerClient dockerClient) {
        this.dockerClient = Objects.requireNonNull(dockerClient, "dockerClient");
    }

    void ensureImage(String imageName, boolean refreshRequested) {
        if (refreshRequested || !dockerClient.imageExists(imageName)) {
            dockerClient.buildImage(imageName);
        }
    }
}
