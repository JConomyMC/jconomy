package org.jconomy.integration.spigot.harness;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class SpigotServerLifecycleManager {

    private static final List<String> FATAL_LOG_PATTERNS = List.of(
            "FAILED TO BIND TO PORT!",
            "Address already in use"
    );

    private final ContainerRuntime containerRuntime;
    private final CommandExecutor commandExecutor;
    private final String imageName;
    private final Duration startupTimeout;
    private final Duration shutdownTimeout;
    private final Duration pollInterval;

    SpigotServerLifecycleManager(
            ContainerRuntime containerRuntime,
            CommandExecutor commandExecutor,
            String imageName,
            Duration startupTimeout,
            Duration shutdownTimeout,
            Duration pollInterval
    ) {
        this.containerRuntime = Objects.requireNonNull(containerRuntime, "containerRuntime");
        this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor");
        this.imageName = Objects.requireNonNull(imageName, "imageName");
        this.startupTimeout = Objects.requireNonNull(startupTimeout, "startupTimeout");
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        this.pollInterval = Objects.requireNonNull(pollInterval, "pollInterval");
    }

    RunningSpigotServer start(RunWorkspace workspace) {
        String containerName = "jconomy-it-" + UUID.randomUUID();
        containerRuntime.start(containerName, imageName, workspace);

        long deadline = System.nanoTime() + startupTimeout.toNanos();
        while (System.nanoTime() < deadline) {
            List<String> logs = containerRuntime.readLogs(containerName);
            String fatalPattern = fatalPattern(logs);
            if (fatalPattern != null) {
                throw new IllegalStateException("Fatal startup log detected: " + fatalPattern);
            }

            if (!containerRuntime.isRunning(containerName)) {
                throw new IllegalStateException("Spigot container exited before readiness was reached.");
            }

            if (containsDoneMarker(logs) && waitForRconHealthCheck(deadline)) {
                return new RunningSpigotServer(containerName, workspace);
            }

            pause();
        }

        throw new IllegalStateException("Startup timeout exceeded while waiting for Spigot readiness.");
    }

    void stop(RunningSpigotServer server) {
        commandExecutor.execute("stop");
        boolean exited = containerRuntime.awaitExit(server.containerName(), shutdownTimeout);
        if (!exited) {
            containerRuntime.kill(server.containerName());
        }
    }

    private boolean waitForRconHealthCheck(long deadline) {
        while (System.nanoTime() < deadline) {
            CommandResult result = commandExecutor.execute("version");
            if (result.success()) {
                return true;
            }
            pause();
        }

        return false;
    }

    private boolean containsDoneMarker(List<String> logs) {
        return logs.stream().anyMatch(line -> line.contains("Done"));
    }

    private String fatalPattern(List<String> logs) {
        for (String line : logs) {
            for (String pattern : FATAL_LOG_PATTERNS) {
                if (line.contains(pattern)) {
                    return pattern;
                }
            }
        }

        return null;
    }

    private void pause() {
        try {
            Thread.sleep(pollInterval.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Spigot lifecycle event.", exception);
        }
    }
}
