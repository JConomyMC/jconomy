package org.jconomy.extensions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;

import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
