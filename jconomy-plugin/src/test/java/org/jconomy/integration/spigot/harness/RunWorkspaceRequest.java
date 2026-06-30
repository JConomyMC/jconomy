package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;
import java.util.List;

record RunWorkspaceRequest(
        String testId,
        Path cachedSpigotJar,
        Path jconomyPluginJar,
        List<String> knownPlayers
) {
}
