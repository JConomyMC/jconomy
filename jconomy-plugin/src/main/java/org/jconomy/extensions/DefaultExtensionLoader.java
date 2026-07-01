package org.jconomy.extensions;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

public class DefaultExtensionLoader implements ExtensionLoader {
    private final JavaPlugin plugin;
    private final ClassLoader classLoader;
    private final DefaultExtensionJarDiscovery jarDiscovery;
    private final DefaultExtensionProviderDiscovery providerDiscovery;

    public DefaultExtensionLoader(JavaPlugin plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;
        this.jarDiscovery = new DefaultExtensionJarDiscovery(plugin);
        this.providerDiscovery = new DefaultExtensionProviderDiscovery(plugin);
    }

    @Override
    public Set<LoadedExtension> load() {
        var jars = jarDiscovery.discover();
        if (jars.isEmpty())
            return Set.of();

        var loadedExtensions = new LinkedHashSet<LoadedExtension>();
        var emptyJarCount = 0;

        for (File jar : jars) {
            try {
                var extensions = loadExtension(jar);
                if (extensions.isEmpty()) {
                    emptyJarCount++;
                }
                loadedExtensions.addAll(extensions);
            } catch (Exception ex) {
                plugin.getLogger().warning(String.format("Failed to load extensions from '%s': %s", jar.getName(), ex.getMessage()));
            }
        }

        var duplicateNameCount = warnOnDuplicateExtensionNames(loadedExtensions);
        var invalidNameCount = countInvalidExtensionNames(loadedExtensions);

        plugin.getLogger().info(String.format("Loaded %d extension(s) from %d jar(s) (empty jars: %d)",
            loadedExtensions.size(), jars.size(), emptyJarCount));
        plugin.getLogger().info(String.format("Load diagnostics: empty jars=%d, duplicate names=%d",
                emptyJarCount, duplicateNameCount));
        plugin.getLogger().info(String.format("Name diagnostics: invalid names=%d", invalidNameCount));

        return loadedExtensions;
    }

    private int countInvalidExtensionNames(Set<LoadedExtension> loadedExtensions) {
        var invalidNameCount = 0;

        for (var loaded : loadedExtensions) {
            var extensionName = loaded.extension().getName();
            if (extensionName == null || extensionName.isBlank()) {
                invalidNameCount++;
            }
        }

        return invalidNameCount;
    }

    private int warnOnDuplicateExtensionNames(Set<LoadedExtension> loadedExtensions) {
        var nameCounts = new LinkedHashMap<String, Integer>();
        for (var loaded : loadedExtensions) {
            var extensionName = loaded.extension().getName();
            nameCounts.put(extensionName, nameCounts.getOrDefault(extensionName, 0) + 1);
        }

        var duplicateNameCount = 0;

        nameCounts.forEach((name, count) -> {
            if (count > 1) {
                plugin.getLogger().warning(String.format("Duplicate extension name detected: %s (count=%d)", name, count));
            }
        });

        for (var count : nameCounts.values()) {
            if (count > 1) {
                duplicateNameCount++;
            }
        }

        return duplicateNameCount;
    }

    private Set<LoadedExtension> loadExtension(File jar) throws Exception {
        plugin.getLogger().info("Loading extension: " + jar.getName());

        var extensions = new LinkedHashSet<LoadedExtension>();
        var urlClassLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, classLoader);
        var hasLoadedExtensions = false;

        try {
            var providers = providerDiscovery.discover(urlClassLoader, jar);

            for (var extension : providers) {
                var extensionName = extension.getName();
                if (extensionName == null) {
                    plugin.getLogger().warning(String.format(
                            "Loaded extension reported null name: %s (jar=%s)",
                            extension.getClass().getName(),
                            jar.getName()));
                } else if (extensionName.isBlank()) {
                    plugin.getLogger().warning(String.format(
                            "Loaded extension reported blank name: %s (jar=%s)",
                            extension.getClass().getName(),
                            jar.getName()));
                }
                plugin.getLogger().info("Loaded extension: " + extensionName);
                extensions.add(new LoadedExtension(extension, urlClassLoader));
                hasLoadedExtensions = true;
            }

            if (!hasLoadedExtensions) {
                plugin.getLogger().warning("No discoverable extensions found in jar: " + jar.getName());
            }

            return extensions;
        } finally {
            if (!hasLoadedExtensions) {
                urlClassLoader.close();
            }
        }
    }
}
