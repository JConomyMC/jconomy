package org.jconomy.integration.spigot.harness;

import java.util.List;

record ArtifactLockManifest(
        BuildToolsArtifact buildTools,
        SpigotArtifact spigot,
        List<PluginArtifact> plugins
) {

    record BuildToolsArtifact(
            String version,
            String url,
            String sha256
    ) {
    }

    record SpigotArtifact(
            String version,
            String buildToolsRevision
    ) {
    }

    record PluginArtifact(
            String name,
            String version,
            String url,
            String sha256
    ) {
    }
}
