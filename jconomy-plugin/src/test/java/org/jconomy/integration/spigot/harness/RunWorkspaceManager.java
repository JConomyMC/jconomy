package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class RunWorkspaceManager {

    private static final String USER_CACHE_EXPIRY = "2036-01-01 00:00:00 +0000";

    private final Path runRoot;
    private final PortAllocator portAllocator;

    RunWorkspaceManager(Path runRoot, PortAllocator portAllocator) {
        this.runRoot = Objects.requireNonNull(runRoot, "runRoot");
        this.portAllocator = Objects.requireNonNull(portAllocator, "portAllocator");
    }

    RunWorkspace createWorkspace(RunWorkspaceRequest request) {
        Objects.requireNonNull(request, "request");

        Path runDirectory = runRoot.resolve(request.testId());
        recreateDirectory(runDirectory);

        int serverPort = portAllocator.nextPort();
        int rconPort = portAllocator.nextPort();

        createDirectories(runDirectory.resolve("plugins"));
        createDirectories(runDirectory.resolve("plugins/JConomy"));
        createDirectories(runDirectory.resolve("logs"));

        copy(request.cachedSpigotJar(), runDirectory.resolve("server.jar"));
        copy(request.jconomyPluginJar(), runDirectory.resolve("plugins/JConomy.jar"));

        write(runDirectory.resolve("eula.txt"), "eula=true\n");
        write(runDirectory.resolve("server.properties"), serverProperties(serverPort, rconPort));
        write(runDirectory.resolve("usercache.json"), userCacheJson(request.knownPlayers()));
        write(runDirectory.resolve("plugins/JConomy/config.yml"), jconomyConfig());

        return new RunWorkspace(runDirectory, serverPort, rconPort);
    }

    private void recreateDirectory(Path directory) {
        deleteRecursively(directory);
        createDirectories(directory);
    }

    private void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create directory: " + directory, exception);
        }
    }

    private void copy(Path source, Path destination) {
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to copy file from " + source + " to " + destination, exception);
        }
    }

    private void write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write file: " + file, exception);
        }
    }

    private String serverProperties(int serverPort, int rconPort) {
        return String.join("\n", List.of(
                "online-mode=false",
                "",
                "enable-rcon=true",
                "rcon.password=test-password",
                "rcon.port=" + rconPort,
                "",
                "level-type=flat",
                "generate-structures=false",
                "spawn-npcs=false",
                "spawn-animals=false",
                "spawn-monsters=false",
                "view-distance=2",
                "simulation-distance=2",
                "",
                "server-port=" + serverPort,
                ""
        ));
    }

    private String userCacheJson(List<String> players) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int index = 0; index < players.size(); index++) {
            String player = players.get(index);
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(StandardCharsets.UTF_8));

            json.append("  {\n");
            json.append("    \"name\": \"").append(player).append("\",\n");
            json.append("    \"uuid\": \"").append(uuid).append("\",\n");
            json.append("    \"expiresOn\": \"").append(USER_CACHE_EXPIRY).append("\"\n");
            json.append("  }");

            if (index < players.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");
        return json.toString();
    }

    private String jconomyConfig() {
        return String.join("\n", List.of(
                "storage:",
                "  type: sqlite",
                "  sqlite:",
                "    path: /server/plugins/JConomy/jconomy.db",
                ""
        ));
    }

    private void deleteRecursively(Path root) {
        if (!Files.exists(root)) {
            return;
        }

        try (var paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new IllegalStateException("Failed to delete path: " + path, exception);
                }
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to remove directory: " + root, exception);
        }
    }
}
