package org.jconomy.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jconomy.JConomyExtension;

class DefaultExtensionLoaderTests {

    @TempDir
    File tempDir;

    @Test
    void constructor_creates_extensions_folder_under_data_folder() {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        assertTrue(new File(tempDir, "extensions").exists(), "extensions folder should be created");
    }

    @Test
    void load_multiple_extensions_from_same_jar_share_one_classloader() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        var jarFile = new File(extensionsDir, "sample-extensions.jar");
        createJarWithClasses(jarFile,
            List.of(
                "org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class",
                "org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class"),
            List.of(
                FirstTestExtension.class.getName(),
                SecondTestExtension.class.getName()));

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();

        assertTrue(loaded.size() == 2, "expected two extensions to load from one jar");
        var distinctClassLoaders = loaded.stream().map(LoadedExtension::classLoader).distinct().count();
        assertTrue(distinctClassLoaders == 1, "expected one classloader shared by all extensions in a jar");
    }

    @Test
    void load_continues_when_jar_contains_malformed_class() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        var jarFile = new File(extensionsDir, "malformed-class.jar");
        createJarWithClasses(jarFile,
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class"),
            List.of(FirstTestExtension.class.getName()));
        appendJarEntry(jarFile, "broken/Bogus.class", new byte[] { 0x00, 0x01, 0x02 });

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();

        assertTrue(loaded.size() == 1, "expected valid extension to load even when one class is malformed");
        var names = loaded.stream().map(le -> le.extension().getName()).toList();
        assertTrue(names.equals(List.of("first-test-extension")), "expected only the valid extension to be loaded");
    }

    @Test
    void load_orders_jars_lexicographically_for_deterministic_startup() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        var logger = mock(java.util.logging.Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        createJarWithClasses(new File(extensionsDir, "zeta.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class"),
            List.of(SecondTestExtension.class.getName()));
        createJarWithClasses(new File(extensionsDir, "alpha.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class"),
            List.of(FirstTestExtension.class.getName()));

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());
        loader.load();

        var loadingMessages = new ArrayList<String>();
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(logger, atLeast(2)).info(captor.capture());
        for (String message : captor.getAllValues()) {
            if (message.startsWith("Loading extension: ")) {
                loadingMessages.add(message);
            }
        }

        assertTrue(loadingMessages.size() == 2, "expected two loading log entries");
        assertTrue(loadingMessages.get(0).equals("Loading extension: alpha.jar"),
                "expected alpha.jar to load first for deterministic ordering");
        assertTrue(loadingMessages.get(1).equals("Loading extension: zeta.jar"),
                "expected zeta.jar to load second for deterministic ordering");
    }

    @Test
    void load_requires_service_descriptor_for_extension_discovery() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        createJarWithClasses(new File(extensionsDir, "with-descriptor.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class"),
            List.of(FirstTestExtension.class.getName()));
        createJarWithClasses(new File(extensionsDir, "without-descriptor.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class"),
            List.of());

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();
        var names = loaded.stream().map(le -> le.extension().getName()).toList();

        assertTrue(names.equals(List.of("first-test-extension")),
            "expected only descriptor-declared extension to be loaded");
    }

    @Test
    void load_logs_startup_summary_with_loaded_count() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        var logger = mock(java.util.logging.Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        createJarWithClasses(new File(extensionsDir, "one.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class"),
            List.of(FirstTestExtension.class.getName()));
        createJarWithClasses(new File(extensionsDir, "two.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class"),
            List.of());

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        loader.load();

        verify(logger).info("Loaded 1 extension(s) from 2 jar(s) (empty jars: 1)");
    }

    @Test
    void load_warns_when_duplicate_extension_names_are_discovered() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        var logger = mock(java.util.logging.Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        createJarWithClasses(new File(extensionsDir, "one.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$DuplicateNameExtensionOne.class"),
            List.of(DuplicateNameExtensionOne.class.getName()));
        createJarWithClasses(new File(extensionsDir, "two.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$DuplicateNameExtensionTwo.class"),
            List.of(DuplicateNameExtensionTwo.class.getName()));

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();

        assertTrue(loaded.size() == 2, "expected both extensions to be loaded despite duplicate names");
        verify(logger).warning(contains("Duplicate extension name detected: duplicate-extension"));
    }

    @Test
    void load_warns_when_jar_contains_no_discoverable_extensions() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);
        var logger = mock(java.util.logging.Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        var extensionsDir = new File(tempDir, "extensions");
        assertTrue(extensionsDir.mkdirs() || extensionsDir.exists());

        createJarWithClasses(new File(extensionsDir, "with-descriptor.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class"),
            List.of(FirstTestExtension.class.getName()));
        createJarWithClasses(new File(extensionsDir, "without-descriptor.jar"),
            List.of("org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class"),
            List.of());

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();

        assertTrue(loaded.size() == 1, "expected only one discoverable extension");
        verify(logger).warning("No discoverable extensions found in jar: without-descriptor.jar");
    }

    private static void createJarWithClasses(File jarFile, List<String> classResourceNames, List<String> providers) throws Exception {
        try (OutputStream output = Files.newOutputStream(jarFile.toPath());
                JarOutputStream jarOutput = new JarOutputStream(output)) {
            for (String resourceName : classResourceNames) {
                jarOutput.putNextEntry(new JarEntry(resourceName));
                try (InputStream input = DefaultExtensionLoaderTests.class.getClassLoader().getResourceAsStream(resourceName)) {
                    if (input == null) {
                        throw new IllegalStateException("Missing class resource: " + resourceName);
                    }
                    input.transferTo(jarOutput);
                }
                jarOutput.closeEntry();
            }

            if (!providers.isEmpty()) {
                jarOutput.putNextEntry(new JarEntry("META-INF/services/org.jconomy.JConomyExtension"));
                var providerContent = String.join("\n", providers) + "\n";
                jarOutput.write(providerContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jarOutput.closeEntry();
            }
        }
    }

    private static void appendJarEntry(File jarFile, String entryName, byte[] content) throws Exception {
        var tempJar = Files.createTempFile("jconomy-extension-test", ".jar");

        try (var originalIn = Files.newInputStream(jarFile.toPath());
                var originalJar = new java.util.jar.JarInputStream(originalIn);
                var out = Files.newOutputStream(tempJar);
                var newJar = new JarOutputStream(out)) {
            JarEntry existing;
            while ((existing = originalJar.getNextJarEntry()) != null) {
                newJar.putNextEntry(new JarEntry(existing.getName()));
                originalJar.transferTo(newJar);
                newJar.closeEntry();
            }

            newJar.putNextEntry(new JarEntry(entryName));
            newJar.write(content);
            newJar.closeEntry();
        }

        Files.move(tempJar, jarFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    public static class FirstTestExtension extends JConomyExtension {
        @Override
        public String getName() {
            return "first-test-extension";
        }
    }

    public static class SecondTestExtension extends JConomyExtension {
        @Override
        public String getName() {
            return "second-test-extension";
        }
    }

    public static class DuplicateNameExtensionOne extends JConomyExtension {
        @Override
        public String getName() {
            return "duplicate-extension";
        }
    }

    public static class DuplicateNameExtensionTwo extends JConomyExtension {
        @Override
        public String getName() {
            return "duplicate-extension";
        }
    }
}
