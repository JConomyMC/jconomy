package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.util.List;

class ProcessDockerClient implements DockerClient {

    @Override
    public boolean imageExists(String imageName) {
        ProcessBuilder builder = new ProcessBuilder(
                List.of("docker", "image", "inspect", imageName)
        );

        return run(builder) == 0;
    }

    @Override
    public void buildImage(String imageName) {
        ProcessBuilder builder = new ProcessBuilder(
                List.of("docker", "build", "-t", imageName, ".")
        );

        int exitCode = run(builder);
        if (exitCode != 0) {
            throw new IllegalStateException("Failed to build Docker image: " + imageName);
        }
    }

    private int run(ProcessBuilder builder) {
        builder.inheritIO();

        try {
            Process process = builder.start();
            return process.waitFor();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to execute Docker command.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker command was interrupted.", exception);
        }
    }
}
