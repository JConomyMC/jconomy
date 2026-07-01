package org.jconomy.extensions;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.bukkit.plugin.java.JavaPlugin;

import org.jconomy.JConomyExtension;

public class DefaultExtensionProviderDiscovery {
    private final JavaPlugin plugin;

    public DefaultExtensionProviderDiscovery(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<JConomyExtension> discover(URLClassLoader classLoader, File jar) {
        var providers = new ArrayList<JConomyExtension>();
        var services = ServiceLoader.load(JConomyExtension.class, classLoader);
        var iterator = services.iterator();

        while (hasNextProvider(iterator, jar)) {
            var extension = loadNextProvider(iterator, jar);
            if (extension.isEmpty()) {
                continue;
            }

            providers.add(extension.get());
        }

        return providers;
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