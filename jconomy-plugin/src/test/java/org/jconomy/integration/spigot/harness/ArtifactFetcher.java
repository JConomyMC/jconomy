package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;

interface ArtifactFetcher {

    void download(String url, Path destination);
}
