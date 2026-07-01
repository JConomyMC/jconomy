package org.jconomy.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
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
                "org/jconomy/extensions/DefaultExtensionLoaderTests$FirstTestExtension.class",
                "org/jconomy/extensions/DefaultExtensionLoaderTests$SecondTestExtension.class");

        var loader = new DefaultExtensionLoader(plugin, getClass().getClassLoader());

        var loaded = loader.load();

        assertTrue(loaded.size() == 2, "expected two extensions to load from one jar");
        var distinctClassLoaders = loaded.stream().map(LoadedExtension::classLoader).distinct().count();
        assertTrue(distinctClassLoaders == 1, "expected one classloader shared by all extensions in a jar");
    }

    private static void createJarWithClasses(File jarFile, String... classResourceNames) throws Exception {
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
        }
    }

    public static class FirstTestExtension implements JConomyExtension {
        @Override
        public String getName() {
            return "first-test-extension";
        }
    }

    public static class SecondTestExtension implements JConomyExtension {
        @Override
        public String getName() {
            return "second-test-extension";
        }
    }
}
