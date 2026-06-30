package org.jconomy.integration.spigot.harness;

import java.time.Duration;
import java.util.List;

interface ContainerRuntime {

    void start(String containerName, String imageName, RunWorkspace workspace);

    boolean isRunning(String containerName);

    List<String> readLogs(String containerName);

    boolean awaitExit(String containerName, Duration timeout);

    void kill(String containerName);
}
