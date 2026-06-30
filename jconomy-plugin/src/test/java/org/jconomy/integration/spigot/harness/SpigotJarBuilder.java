package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;

interface SpigotJarBuilder {

    Path buildSpigotJar(Path buildToolsJar, String spigotVersion, Path workspace);
}
