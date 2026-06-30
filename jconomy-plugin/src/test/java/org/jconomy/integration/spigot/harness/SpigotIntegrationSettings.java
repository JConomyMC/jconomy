package org.jconomy.integration.spigot.harness;

import java.nio.file.Path;
import java.util.Map;

record SpigotIntegrationSettings(
        Path cacheRoot,
        Path runRoot,
        boolean keepRuns,
        String spigotVersion,
        String dockerImageName
) {

    private static final String CACHE_PROPERTY = "jellyrekt.test.cache";
    private static final String CACHE_ENVIRONMENT = "JELLYREKT_TEST_CACHE";
    private static final String KEEP_RUNS_PROPERTY = "jconomy.integration.keepRuns";
    private static final String RUN_ROOT_PROPERTY = "jconomy.integration.runRoot";
    private static final String SPIGOT_VERSION_PROPERTY = "jconomy.integration.spigot.version";
    private static final String DOCKER_IMAGE_PROPERTY = "jconomy.integration.docker.image";

    private static final String DEFAULT_SPIGOT_VERSION = "1.21.8";
    private static final String DEFAULT_DOCKER_IMAGE = "jellyrekt-minecraft-test-runtime:latest";

    static SpigotIntegrationSettings fromSystem() {
        return resolve(System.getProperties().stringPropertyNames().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                name -> name,
                                System::getProperty
                        )),
                System.getenv(),
                Path.of(System.getProperty("user.home"))
        );
    }

    static SpigotIntegrationSettings resolve(
            Map<String, String> properties,
            Map<String, String> environment,
            Path userHome
    ) {
        Path cacheRoot = resolveCacheRoot(properties, environment, userHome);
        Path runRoot = resolvePath(properties, RUN_ROOT_PROPERTY).orElse(Path.of("target/integration-runs"));
        boolean keepRuns = Boolean.parseBoolean(properties.getOrDefault(KEEP_RUNS_PROPERTY, "false"));
        String spigotVersion = resolveString(properties, SPIGOT_VERSION_PROPERTY).orElse(DEFAULT_SPIGOT_VERSION);
        String dockerImageName = resolveString(properties, DOCKER_IMAGE_PROPERTY).orElse(DEFAULT_DOCKER_IMAGE);

        return new SpigotIntegrationSettings(cacheRoot, runRoot, keepRuns, spigotVersion, dockerImageName);
    }

    private static Path resolveCacheRoot(
            Map<String, String> properties,
            Map<String, String> environment,
            Path userHome
    ) {
        if (properties.containsKey(CACHE_PROPERTY)) {
            return Path.of(requireNonBlank(properties.get(CACHE_PROPERTY), CACHE_PROPERTY));
        }

        if (environment.containsKey(CACHE_ENVIRONMENT)) {
            return Path.of(requireNonBlank(environment.get(CACHE_ENVIRONMENT), CACHE_ENVIRONMENT));
        }

        return userHome.resolve(".jellyrekt/test-cache");
    }

    private static java.util.Optional<Path> resolvePath(Map<String, String> properties, String key) {
        if (!properties.containsKey(key)) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(Path.of(requireNonBlank(properties.get(key), key)));
    }

    private static java.util.Optional<String> resolveString(Map<String, String> properties, String key) {
        if (!properties.containsKey(key)) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(requireNonBlank(properties.get(key), key));
    }

    private static String requireNonBlank(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " cannot be blank.");
        }

        return value.trim();
    }
}
