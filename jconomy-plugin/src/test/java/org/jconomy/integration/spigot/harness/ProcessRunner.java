package org.jconomy.integration.spigot.harness;

import java.time.Duration;
import java.util.List;

interface ProcessRunner {

    ProcessResult run(List<String> command, Duration timeout);
}
