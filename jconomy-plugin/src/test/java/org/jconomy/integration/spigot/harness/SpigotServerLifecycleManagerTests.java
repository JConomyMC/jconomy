package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpigotServerLifecycleManagerTests {

    @TempDir
    Path tempDir;

    @Test
    void startWaitsForDoneLogAndRconHealthCheck() {
        RecordingContainerRuntime runtime = new RecordingContainerRuntime();
        runtime.running = true;
        runtime.logs = List.of("[Server thread/INFO]: Done (2.100s)! For help, type \"help\"");

        RecordingCommandExecutor executor = new RecordingCommandExecutor(
                List.of(
                        new CommandResult(false, "connection refused"),
                        new CommandResult(true, "This server is running Spigot")
                )
        );

        SpigotServerLifecycleManager manager = new SpigotServerLifecycleManager(
                runtime,
                executor,
                "jellyrekt-minecraft-test-runtime:latest",
                Duration.ofSeconds(2),
                Duration.ofSeconds(1),
                Duration.ofMillis(5)
        );

        RunWorkspace workspace = new RunWorkspace(tempDir.resolve("run"), 25565, 25575);
        RunningSpigotServer server = manager.start(workspace);

        assertNotNull(server);
        assertTrue(server.containerName().startsWith("jconomy-it-"));
        assertEquals("jellyrekt-minecraft-test-runtime:latest", runtime.startedImageName);
        assertEquals(workspace, runtime.startedWorkspace);
        assertEquals(List.of("version", "version"), executor.commands);
    }

    @Test
    void startFailsWhenContainerExitsBeforeReadiness() {
        RecordingContainerRuntime runtime = new RecordingContainerRuntime();
        runtime.running = false;
        runtime.logs = List.of();

        SpigotServerLifecycleManager manager = new SpigotServerLifecycleManager(
                runtime,
                new RecordingCommandExecutor(List.of()),
                "image",
                Duration.ofMillis(100),
                Duration.ofMillis(100),
                Duration.ofMillis(5)
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> manager.start(new RunWorkspace(tempDir.resolve("run"), 25565, 25575))
        );

        assertEquals("Spigot container exited before readiness was reached.", error.getMessage());
    }

    @Test
    void startFailsWhenFatalStartupLogAppears() {
        RecordingContainerRuntime runtime = new RecordingContainerRuntime();
        runtime.running = true;
        runtime.logs = List.of("[Server thread/ERROR]: FAILED TO BIND TO PORT!");

        SpigotServerLifecycleManager manager = new SpigotServerLifecycleManager(
                runtime,
                new RecordingCommandExecutor(List.of()),
                "image",
                Duration.ofMillis(100),
                Duration.ofMillis(100),
                Duration.ofMillis(5)
        );

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> manager.start(new RunWorkspace(tempDir.resolve("run"), 25565, 25575))
        );

        assertEquals("Fatal startup log detected: FAILED TO BIND TO PORT!", error.getMessage());
    }

    @Test
    void stopKillsContainerWhenGracefulStopTimesOut() {
        RecordingContainerRuntime runtime = new RecordingContainerRuntime();
        runtime.awaitExitResult = false;
        RecordingCommandExecutor executor = new RecordingCommandExecutor(List.of(new CommandResult(true, "Stopping")));

        SpigotServerLifecycleManager manager = new SpigotServerLifecycleManager(
                runtime,
                executor,
                "image",
                Duration.ofSeconds(1),
                Duration.ofMillis(50),
                Duration.ofMillis(5)
        );

        manager.stop(new RunningSpigotServer("jconomy-it-123", new RunWorkspace(tempDir.resolve("run"), 25565, 25575)));

        assertEquals(List.of("stop"), executor.commands);
        assertTrue(runtime.killCalled);
    }

    @Test
    void stopDoesNotKillContainerWhenGracefulStopSucceeds() {
        RecordingContainerRuntime runtime = new RecordingContainerRuntime();
        runtime.awaitExitResult = true;
        RecordingCommandExecutor executor = new RecordingCommandExecutor(List.of(new CommandResult(true, "Stopping")));

        SpigotServerLifecycleManager manager = new SpigotServerLifecycleManager(
                runtime,
                executor,
                "image",
                Duration.ofSeconds(1),
                Duration.ofMillis(50),
                Duration.ofMillis(5)
        );

        manager.stop(new RunningSpigotServer("jconomy-it-123", new RunWorkspace(tempDir.resolve("run"), 25565, 25575)));

        assertFalse(runtime.killCalled);
    }

    private static final class RecordingContainerRuntime implements ContainerRuntime {

        private boolean running;
        private List<String> logs = List.of();
        private RunWorkspace startedWorkspace;
        private String startedImageName;
        private boolean awaitExitResult;
        private boolean killCalled;

        @Override
        public void start(String containerName, String imageName, RunWorkspace workspace) {
            this.startedImageName = imageName;
            this.startedWorkspace = workspace;
        }

        @Override
        public boolean isRunning(String containerName) {
            return running;
        }

        @Override
        public List<String> readLogs(String containerName) {
            return logs;
        }

        @Override
        public boolean awaitExit(String containerName, Duration timeout) {
            return awaitExitResult;
        }

        @Override
        public void kill(String containerName) {
            killCalled = true;
        }
    }

    private static final class RecordingCommandExecutor implements CommandExecutor {

        private final List<CommandResult> responses;
        private final List<String> commands = new ArrayList<>();
        private int index;

        private RecordingCommandExecutor(List<CommandResult> responses) {
            this.responses = responses;
        }

        @Override
        public CommandResult execute(String command) {
            commands.add(command);
            if (responses.isEmpty()) {
                return new CommandResult(false, "no response configured");
            }

            if (index >= responses.size()) {
                return responses.getLast();
            }

            return responses.get(index++);
        }
    }
}
