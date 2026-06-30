package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;
import java.util.Objects;

class ManifestBuildToolsDownloader implements BuildToolsDownloader {

    private final ArtifactLockManifest manifest;
    private final ArtifactFetcher artifactFetcher;

    ManifestBuildToolsDownloader(ArtifactLockManifest manifest, ArtifactFetcher artifactFetcher) {
        this.manifest = Objects.requireNonNull(manifest, "manifest");
        this.artifactFetcher = Objects.requireNonNull(artifactFetcher, "artifactFetcher");
    }

    @Override
    public void download(Path destination) {
        ArtifactLockManifest.BuildToolsArtifact buildTools = manifest.buildTools();
        if (buildTools == null) {
            throw new IllegalStateException("Manifest is missing buildTools artifact definition.");
        }

        artifactFetcher.download(buildTools.url(), destination);
        Sha256Verifier.verify(destination, buildTools.sha256());
    }
}
