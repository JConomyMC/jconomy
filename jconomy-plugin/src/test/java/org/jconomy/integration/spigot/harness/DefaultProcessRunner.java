package org.jconomy.integration.spigot.harness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

class DefaultProcessRunner implements ProcessRunner {

    @Override
    public ProcessResult run(List<String> command, Duration timeout) {
        ProcessBuilder builder = new ProcessBuilder(command);

        try {
            Process process = builder.start();
            boolean finished = process.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new ProcessResult(-1, "", "Process timed out.", true);
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), stdout, stderr, false);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start process: " + String.join(" ", command), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for process: " + String.join(" ", command), exception);
        }
    }
}
