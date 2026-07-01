package org.jconomy.extensions;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.bukkit.plugin.java.JavaPlugin;

import org.jconomy.JConomyExtension;

public class DefaultExtensionLoader implements ExtensionLoader {
    private final JavaPlugin plugin;
    private final ClassLoader classLoader;
    private final DefaultExtensionJarDiscovery jarDiscovery;

    public DefaultExtensionLoader(JavaPlugin plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;
        this.jarDiscovery = new DefaultExtensionJarDiscovery(plugin);
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
            var services = ServiceLoader.load(JConomyExtension.class, urlClassLoader);
            var iterator = services.iterator();

            while (hasNextProvider(iterator, jar)) {
                var extension = loadNextProvider(iterator, jar);
                if (extension.isEmpty()) {
                    continue;
                }

                plugin.getLogger().info("Loaded extension: " + extension.get().getName());
                extensions.add(new LoadedExtension(extension.get(), urlClassLoader));
                hasLoadedExtensions = true;
            }

            return extensions;
        } finally {
            if (!hasLoadedExtensions) {
                urlClassLoader.close();
            }
        }
    }

    private boolean hasNextProvider(Iterator<JConomyExtension> iterator, File jar) {
        try {
            return iterator.hasNext();
        } catch (Throwable ex) {
            plugin.getLogger().warning(String.format("Failed to discover extension providers in '%s': %s", jar.getName(), ex.getMessage()));
            return false;
        }
    }

    private Optional<JConomyExtension> loadNextProvider(Iterator<JConomyExtension> iterator, File jar) {
        try {
            return Optional.of(iterator.next());
        } catch (Throwable ex) {
            plugin.getLogger().warning(String.format("Failed to instantiate extension provider in '%s': %s", jar.getName(), ex.getMessage()));
            return Optional.empty();
        }
    }
}
