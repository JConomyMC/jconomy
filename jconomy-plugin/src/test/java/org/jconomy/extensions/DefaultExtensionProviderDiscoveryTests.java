package org.jconomy.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jconomy.JConomyExtension;

class DefaultExtensionProviderDiscoveryTests {

    @TempDir
    File tempDir;

    @Test
    void discover_returns_all_service_loader_providers_from_jar() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        var jarFile = new File(tempDir, "extensions.jar");
        createJarWithClasses(jarFile,
                List.of(
                        "org/jconomy/extensions/DefaultExtensionProviderDiscoveryTests$FirstProvider.class",
                        "org/jconomy/extensions/DefaultExtensionProviderDiscoveryTests$SecondProvider.class"),
                List.of(FirstProvider.class.getName(), SecondProvider.class.getName()));

        try (var classLoader = new java.net.URLClassLoader(new java.net.URL[] { jarFile.toURI().toURL() }, getClass().getClassLoader())) {
            var discovery = new DefaultExtensionProviderDiscovery(plugin);

            var providers = discovery.discover(classLoader, jarFile);

            assertTrue(providers.size() == 2, "expected both providers to be discovered");
            assertTrue(providers.stream().map(JConomyExtension::getName).toList().equals(List.of("first-provider", "second-provider")));
        }
    }

    @Test
    void discover_continues_when_one_provider_fails_to_instantiate() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        var jarFile = new File(tempDir, "extensions.jar");
        createJarWithClasses(jarFile,
                List.of(
                        "org/jconomy/extensions/DefaultExtensionProviderDiscoveryTests$BrokenProvider.class",
                        "org/jconomy/extensions/DefaultExtensionProviderDiscoveryTests$SecondProvider.class"),
                List.of(BrokenProvider.class.getName(), SecondProvider.class.getName()));

        try (var classLoader = new java.net.URLClassLoader(new java.net.URL[] { jarFile.toURI().toURL() }, getClass().getClassLoader())) {
            var discovery = new DefaultExtensionProviderDiscovery(plugin);

            var providers = discovery.discover(classLoader, jarFile);

            assertTrue(providers.size() == 1, "expected discovery to continue after a bad provider");
            assertTrue(providers.stream().map(JConomyExtension::getName).toList().equals(List.of("second-provider")));
        }
    }

    private static void createJarWithClasses(File jarFile, List<String> classResourceNames, List<String> providers) throws Exception {
        try (OutputStream output = Files.newOutputStream(jarFile.toPath());
                JarOutputStream jarOutput = new JarOutputStream(output)) {
            for (String resourceName : classResourceNames) {
                jarOutput.putNextEntry(new JarEntry(resourceName));
                try (InputStream input = DefaultExtensionProviderDiscoveryTests.class.getClassLoader().getResourceAsStream(resourceName)) {
                    if (input == null) {
                        throw new IllegalStateException("Missing class resource: " + resourceName);
                    }
                    input.transferTo(jarOutput);
                }
                jarOutput.closeEntry();
            }

            jarOutput.putNextEntry(new JarEntry("META-INF/services/org.jconomy.JConomyExtension"));
            var providerContent = String.join("\n", providers) + "\n";
            jarOutput.write(providerContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jarOutput.closeEntry();
        }
    }

    public static class FirstProvider implements JConomyExtension {
        @Override
        public String getName() {
            return "first-provider";
        }
    }

    public static class SecondProvider implements JConomyExtension {
        @Override
        public String getName() {
            return "second-provider";
        }
    }

    public static class BrokenProvider implements JConomyExtension {
        public BrokenProvider() {
            throw new IllegalStateException("broken");
        }

        @Override
        public String getName() {
            return "broken-provider";
        }
    }
}