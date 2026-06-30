package org.jconomy.integration.spigot.harness;

record ProcessResult(
        int exitCode,
        String stdout,
        String stderr,
        boolean timedOut
) {

    boolean isSuccess() {
        return exitCode == 0 && !timedOut;
    }
}
