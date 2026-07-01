package org.jconomy.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultExtensionJarDiscoveryTests {

    @TempDir
    File tempDir;

    @Test
    void discover_returns_jar_files_in_lexicographic_order() throws Exception {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);

        var extensionFolder = new File(tempDir, "extensions");
        assertTrue(extensionFolder.mkdirs() || extensionFolder.exists());

        Files.createFile(new File(extensionFolder, "zeta.jar").toPath());
        Files.createFile(new File(extensionFolder, "alpha.jar").toPath());
        Files.createFile(new File(extensionFolder, "notes.txt").toPath());

        var discovery = new DefaultExtensionJarDiscovery(plugin);

        var jars = discovery.discover();

        assertTrue(jars.stream().map(File::getName).toList().equals(List.of("alpha.jar", "zeta.jar")),
                "expected jars to be returned in sorted order and non-jars ignored");
    }

    @Test
    void discover_creates_extensions_folder_if_missing() {
        var plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn(tempDir);

        var discovery = new DefaultExtensionJarDiscovery(plugin);

        assertTrue(new File(tempDir, "extensions").exists(), "expected extensions folder to be created");

        discovery.discover();
    }
}