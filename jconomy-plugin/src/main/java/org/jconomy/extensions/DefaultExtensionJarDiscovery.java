package org.jconomy.extensions;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class DefaultExtensionJarDiscovery {
    private final File extensionFolder;

    public DefaultExtensionJarDiscovery(JavaPlugin plugin) {
        this.extensionFolder = new File(plugin.getDataFolder(), "extensions");
        if (!extensionFolder.exists()) {
            extensionFolder.mkdirs();
        }
    }

    public List<File> discover() {
        var jars = extensionFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) {
            return List.of();
        }

        Arrays.sort(jars, Comparator.comparing(File::getName));
        return List.copyOf(Arrays.asList(jars));
    }
}