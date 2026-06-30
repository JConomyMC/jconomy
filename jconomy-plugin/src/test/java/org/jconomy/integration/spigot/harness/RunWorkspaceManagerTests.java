package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunWorkspaceManagerTests {

    @TempDir
    Path tempDir;

    @Test
    void createWorkspaceRecreatesRunDirectoryAndCopiesArtifacts() throws IOException {
        Path runRoot = tempDir.resolve("target/integration-runs");
        Path staleDirectory = runRoot.resolve("jconomy-spigot-basic");
        Files.createDirectories(staleDirectory);
        Files.writeString(staleDirectory.resolve("stale.txt"), "old");

        Path cachedSpigotJar = tempDir.resolve("cache/spigot.jar");
        Path pluginJar = tempDir.resolve("build/JConomy.jar");
        Files.createDirectories(cachedSpigotJar.getParent());
        Files.createDirectories(pluginJar.getParent());
        Files.writeString(cachedSpigotJar, "spigot");
        Files.writeString(pluginJar, "plugin");

        RunWorkspaceManager manager = new RunWorkspaceManager(runRoot, new FixedPortAllocator(25565, 25575));
        RunWorkspace workspace = manager.createWorkspace(new RunWorkspaceRequest(
                "jconomy-spigot-basic",
                cachedSpigotJar,
                pluginJar,
                List.of("Alice", "Bob")
        ));

        assertFalse(Files.exists(workspace.runDirectory().resolve("stale.txt")));
        assertEquals("spigot", Files.readString(workspace.runDirectory().resolve("server.jar")));
        assertEquals("plugin", Files.readString(workspace.runDirectory().resolve("plugins/JConomy.jar")));
    }

    @Test
    void createWorkspaceGeneratesCoreServerFilesAndKnownPlayerCache() throws IOException {
        Path cachedSpigotJar = tempDir.resolve("cache/spigot.jar");
        Path pluginJar = tempDir.resolve("build/JConomy.jar");
        Files.createDirectories(cachedSpigotJar.getParent());
        Files.createDirectories(pluginJar.getParent());
        Files.writeString(cachedSpigotJar, "spigot");
        Files.writeString(pluginJar, "plugin");

        RunWorkspaceManager manager = new RunWorkspaceManager(tempDir.resolve("target/integration-runs"), new FixedPortAllocator(25565, 25575));
        RunWorkspace workspace = manager.createWorkspace(new RunWorkspaceRequest(
                "jconomy-spigot-basic",
                cachedSpigotJar,
                pluginJar,
                List.of("Alice", "Bob")
        ));

        assertEquals("eula=true\n", Files.readString(workspace.runDirectory().resolve("eula.txt")));

        String serverProperties = Files.readString(workspace.runDirectory().resolve("server.properties"));
        assertTrue(serverProperties.contains("online-mode=false"));
        assertTrue(serverProperties.contains("rcon.port=25575"));
        assertTrue(serverProperties.contains("server-port=25565"));

        String userCache = Files.readString(workspace.runDirectory().resolve("usercache.json"));
        assertTrue(userCache.contains("\"name\": \"Alice\""));
        assertTrue(userCache.contains("\"name\": \"Bob\""));
        assertFalse(userCache.contains("Mallory"));

        String jconomyConfig = Files.readString(workspace.runDirectory().resolve("plugins/JConomy/config.yml"));
        assertTrue(jconomyConfig.contains("/server/plugins/JConomy/jconomy.db"));
    }

    @Test
    void createWorkspaceExposesAllocatedPorts() throws IOException {
        Path cachedSpigotJar = tempDir.resolve("cache/spigot.jar");
        Path pluginJar = tempDir.resolve("build/JConomy.jar");
        Files.createDirectories(cachedSpigotJar.getParent());
        Files.createDirectories(pluginJar.getParent());
        Files.writeString(cachedSpigotJar, "spigot");
        Files.writeString(pluginJar, "plugin");

        RunWorkspaceManager manager = new RunWorkspaceManager(tempDir.resolve("target/integration-runs"), new FixedPortAllocator(24444, 25599));
        RunWorkspace workspace = manager.createWorkspace(new RunWorkspaceRequest(
                "jconomy-spigot-ports",
                cachedSpigotJar,
                pluginJar,
                List.of("Alice")
        ));

        assertEquals(24444, workspace.serverPort());
        assertEquals(25599, workspace.rconPort());
    }

    private static final class FixedPortAllocator implements PortAllocator {

        private final int serverPort;
        private final int rconPort;

        private FixedPortAllocator(int serverPort, int rconPort) {
            this.serverPort = serverPort;
            this.rconPort = rconPort;
        }

        @Override
        public int nextPort() {
            if (serverPort == rconPort) {
                return serverPort;
            }

            if (invocations == 0) {
                invocations++;
                return serverPort;
            }

            return rconPort;
        }

        private int invocations;
    }
}
