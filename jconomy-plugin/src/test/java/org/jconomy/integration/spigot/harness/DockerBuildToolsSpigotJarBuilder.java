package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

class DockerBuildToolsSpigotJarBuilder implements SpigotJarBuilder {

    private final ProcessRunner processRunner;
    private final String runtimeImageName;

    DockerBuildToolsSpigotJarBuilder(ProcessRunner processRunner, String runtimeImageName) {
        this.processRunner = Objects.requireNonNull(processRunner, "processRunner");
        this.runtimeImageName = Objects.requireNonNull(runtimeImageName, "runtimeImageName");
    }

    @Override
    public Path buildSpigotJar(Path buildToolsJar, String spigotVersion, Path workspace) {
        Path buildToolsInWorkspace = workspace.resolve("BuildTools.jar");
        try {
            java.nio.file.Files.createDirectories(workspace);
            java.nio.file.Files.copy(buildToolsJar, buildToolsInWorkspace, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to prepare BuildTools workspace.", exception);
        }

        ProcessResult result = processRunner.run(
                List.of(
                        "docker", "run", "--rm",
                        "-v", workspace + ":/build",
                        "-w", "/build",
                        runtimeImageName,
                        "java", "-jar", "BuildTools.jar", "--rev", spigotVersion
                ),
                Duration.ofMinutes(30)
        );

        if (!result.isSuccess()) {
            throw new IllegalStateException("BuildTools failed to build Spigot jar.");
        }

        Path expectedSpigotJar = workspace.resolve("spigot-" + spigotVersion + ".jar");
        if (java.nio.file.Files.exists(expectedSpigotJar)) {
            return expectedSpigotJar;
        }

        throw new IllegalStateException("BuildTools completed but no expected Spigot jar was found.");
    }
}
