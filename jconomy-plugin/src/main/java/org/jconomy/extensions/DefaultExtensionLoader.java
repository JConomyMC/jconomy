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

        for (File jar : jars) {
            try {
                var extensions = loadExtension(jar);
                loadedExtensions.addAll(extensions);
            } catch (Exception ex) {
                plugin.getLogger().warning(String.format("Failed to load extensions from '%s': %s", jar.getName(), ex.getMessage()));
            }
        }

        plugin.getLogger().info(String.format("Loaded %d extension(s) from %d jar(s)", loadedExtensions.size(), jars.size()));

        return loadedExtensions;
    }

    private Set<LoadedExtension> loadExtension(File jar) throws Exception {
        plugin.getLogger().info("Loading extension: " + jar.getName());

        var extensions = new LinkedHashSet<LoadedExtension>();
        var urlClassLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, classLoader);
        var hasLoadedExtensions = false;

        try {
            var providers = providerDiscovery.discover(urlClassLoader, jar);

            for (var extension : providers) {
                plugin.getLogger().info("Loaded extension: " + extension.getName());
                extensions.add(new LoadedExtension(extension, urlClassLoader));
                hasLoadedExtensions = true;
            }

            return extensions;
        } finally {
            if (!hasLoadedExtensions) {
                urlClassLoader.close();
            }
        }
    }
}
