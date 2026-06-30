package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DockerPreflightTests {

    @Test
    void verifyDockerAvailablePassesWhenDockerVersionSucceeds() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.nextResult = new ProcessResult(0, "Docker version 27", "", false);

        DockerPreflight.verifyDockerAvailable(runner);

        assertEquals(List.of("docker", "version"), runner.commands.getFirst());
    }

    @Test
    void verifyDockerAvailableFailsFastWhenDockerIsUnavailable() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.nextResult = new ProcessResult(127, "", "docker: command not found", false);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> DockerPreflight.verifyDockerAvailable(runner)
        );

        assertEquals(
                "Docker is required when jconomy.integration.spigot=true. Ensure Docker is installed and running.",
                error.getMessage()
        );
    }

    private static final class RecordingProcessRunner implements ProcessRunner {

        private final List<List<String>> commands = new ArrayList<>();
        private ProcessResult nextResult = new ProcessResult(0, "", "", false);

        @Override
        public ProcessResult run(List<String> command, Duration timeout) {
            commands.add(command);
            return nextResult;
        }
    }
}
