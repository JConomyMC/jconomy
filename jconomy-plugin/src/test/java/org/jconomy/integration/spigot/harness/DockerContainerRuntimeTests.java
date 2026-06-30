package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerContainerRuntimeTests {

    @Test
    void startUsesExpectedDockerRunCommand() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.results.add(new ProcessResult(0, "container-id", "", false));

        DockerContainerRuntime runtime = new DockerContainerRuntime(runner);
        runtime.start("jconomy-it-1", "runtime:image", new RunWorkspace(Path.of("/tmp/run"), 25565, 25575));

        List<String> command = runner.commands.getFirst();
        assertEquals("docker", command.getFirst());
        assertTrue(command.contains("run"));
        assertTrue(command.contains("runtime:image"));
        assertTrue(command.contains("--name"));
        assertTrue(command.contains("jconomy-it-1"));
        assertTrue(command.contains("127.0.0.1:25575:25575"));
    }

    @Test
    void startFailsWhenDockerRunFails() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.results.add(new ProcessResult(1, "", "failed", false));

        DockerContainerRuntime runtime = new DockerContainerRuntime(runner);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> runtime.start("jconomy-it-1", "runtime:image", new RunWorkspace(Path.of("/tmp/run"), 25565, 25575))
        );

        assertEquals("Failed to start Docker container: jconomy-it-1", error.getMessage());
    }

    @Test
    void isRunningReturnsTrueWhenInspectReportsTrue() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.results.add(new ProcessResult(0, "true\n", "", false));

        DockerContainerRuntime runtime = new DockerContainerRuntime(runner);
        assertTrue(runtime.isRunning("jconomy-it-1"));
    }

    @Test
    void awaitExitReturnsFalseOnTimeout() {
        RecordingProcessRunner runner = new RecordingProcessRunner();
        runner.results.add(new ProcessResult(-1, "", "timeout", true));

        DockerContainerRuntime runtime = new DockerContainerRuntime(runner);
        assertFalse(runtime.awaitExit("jconomy-it-1", Duration.ofMillis(5)));
    }

    private static final class RecordingProcessRunner implements ProcessRunner {

        private final List<List<String>> commands = new ArrayList<>();
        private final List<ProcessResult> results = new ArrayList<>();
        private int index;

        @Override
        public ProcessResult run(List<String> command, Duration timeout) {
            commands.add(command);
            if (index >= results.size()) {
                return new ProcessResult(0, "", "", false);
            }
            return results.get(index++);
        }
    }
}
