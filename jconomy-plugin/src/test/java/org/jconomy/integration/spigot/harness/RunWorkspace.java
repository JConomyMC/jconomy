package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;

record RunWorkspace(
        Path runDirectory,
        int serverPort,
        int rconPort
) {
}
