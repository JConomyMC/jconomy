package org.jconomy.integration.spigot.harness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "jconomy.integration.spigot", matches = "true")
class JConomySpigotIT {

    @Test
    void runsBasicJConomyFlowAgainstSpigotServer() {
        SpigotIntegrationSettings settings = SpigotIntegrationSettings.fromSystem();
        DefaultProcessRunner processRunner = new DefaultProcessRunner();
        assertTrue(Boolean.getBoolean("jconomy.integration.spigot"));
        boolean success = false;

        DockerPreflight.verifyDockerAvailable(processRunner);

        ArtifactLockManifest manifest = ArtifactLockManifestLoader.loadFromResource("integration/spigot/artifacts-lock.json");
        if (manifest.buildTools().sha256().contains("REPLACE_WITH_REAL_SHA256")) {
            throw new IllegalStateException("artifacts-lock.json must contain a real BuildTools SHA-256 before running integration tests.");
        }

        RuntimeImageManager imageManager = new RuntimeImageManager(new ProcessDockerClient());
        imageManager.ensureImage(settings.dockerImageName(), false);

        SpigotArtifactCacheManager cacheManager = SpigotArtifactCacheManager.fromManifest(
                settings.cacheRoot(),
                manifest,
                new HttpArtifactFetcher(),
                new DockerBuildToolsSpigotJarBuilder(processRunner, settings.dockerImageName())
        );
        Path spigotJar = cacheManager.ensureSpigotJar();

        Path pluginJar = Path.of("target", "JConomy-1.0-SNAPSHOT.jar");
        if (!Files.exists(pluginJar)) {
            throw new IllegalStateException("Expected plugin artifact is missing: " + pluginJar);
        }

        RunWorkspaceManager workspaceManager = new RunWorkspaceManager(settings.runRoot(), new SystemPortAllocator());
        RunWorkspace workspace = workspaceManager.createWorkspace(new RunWorkspaceRequest(
                "jconomy-spigot-basic",
                spigotJar,
                pluginJar,
                List.of("Alice", "Bob")
        ));

        RconCommandExecutor rcon = new RconCommandExecutor("127.0.0.1", workspace.rconPort(), "test-password", Duration.ofSeconds(5));
        SpigotServerLifecycleManager lifecycle = new SpigotServerLifecycleManager(
            new DockerContainerRuntime(processRunner),
                rcon,
                settings.dockerImageName(),
                Duration.ofMinutes(2),
                Duration.ofSeconds(30),
                Duration.ofMillis(250)
        );

        RunningSpigotServer server = lifecycle.start(workspace);
        try {
            assertTrue(rcon.execute("jconomy account create Alice").success());
            assertTrue(rcon.execute("jconomy account create Bob").success());
            assertTrue(rcon.execute("jconomy balance add Alice default 100").success());
            assertTrue(rcon.execute("jconomy balance get Alice default").success());
            assertFalse(rcon.execute("jconomy account create Mallory").output().isBlank());
        } finally {
            lifecycle.stop(server);
        }

        Path database = workspace.runDirectory().resolve("plugins/JConomy/jconomy.db");
        assertTrue(Files.exists(database));
        JConomyDatabaseAssertions.assertAccountExists(database, "Alice");
        JConomyDatabaseAssertions.assertAccountExists(database, "Bob");
        JConomyDatabaseAssertions.assertAccountMissing(database, "Mallory");
        JConomyDatabaseAssertions.assertBalance(database, "Alice", "world", "default", new BigDecimal("100"));
        success = true;

        RunWorkspaceCleanup.cleanup(workspace.runDirectory(), settings.keepRuns(), success);
    }
}
