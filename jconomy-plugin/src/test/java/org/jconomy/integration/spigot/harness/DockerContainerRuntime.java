package org.jconomy.integration.spigot.harness;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class DockerContainerRuntime implements ContainerRuntime {

    private final ProcessRunner processRunner;

    DockerContainerRuntime(ProcessRunner processRunner) {
        this.processRunner = Objects.requireNonNull(processRunner, "processRunner");
    }

    @Override
    public void start(String containerName, String imageName, RunWorkspace workspace) {
        List<String> command = List.of(
                "docker", "run", "-d", "--rm",
                "--name", containerName,
                "-v", workspace.runDirectory() + ":/server",
                "-w", "/server",
                "-p", "127.0.0.1:" + workspace.rconPort() + ":25575",
                imageName,
                "java", "-jar", "server.jar", "nogui"
        );

        ProcessResult result = processRunner.run(command, Duration.ofMinutes(1));
        if (!result.isSuccess()) {
            throw new IllegalStateException("Failed to start Docker container: " + containerName);
        }
    }

    @Override
    public boolean isRunning(String containerName) {
        ProcessResult result = processRunner.run(
                List.of("docker", "inspect", "-f", "{{.State.Running}}", containerName),
                Duration.ofSeconds(5)
        );

        return result.isSuccess() && result.stdout().trim().equals("true");
    }

    @Override
    public List<String> readLogs(String containerName) {
        ProcessResult result = processRunner.run(
                List.of("docker", "logs", containerName),
                Duration.ofSeconds(5)
        );

        if (!result.isSuccess()) {
            return List.of();
        }

        if (result.stdout().isBlank()) {
            return List.of();
        }

        return Arrays.asList(result.stdout().split("\\R"));
    }

    @Override
    public boolean awaitExit(String containerName, Duration timeout) {
        ProcessResult result = processRunner.run(
                List.of("docker", "wait", containerName),
                timeout
        );

        return result.isSuccess();
    }

    @Override
    public void kill(String containerName) {
        processRunner.run(List.of("docker", "kill", containerName), Duration.ofSeconds(10));
    }
}
