package org.jconomy.integration.spigot.harness;

import java.time.Duration;
import java.util.List;

final class DockerPreflight {

    private DockerPreflight() {
    }

    static void verifyDockerAvailable(ProcessRunner processRunner) {
        ProcessResult result = processRunner.run(List.of("docker", "version"), Duration.ofSeconds(15));
        if (!result.isSuccess()) {
            throw new IllegalStateException(
                    "Docker is required when jconomy.integration.spigot=true. Ensure Docker is installed and running."
            );
        }
    }
}
