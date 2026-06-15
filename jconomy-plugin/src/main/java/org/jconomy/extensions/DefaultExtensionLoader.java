package org.jconomy.extensions;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.plugin.java.JavaPlugin;

import org.jconomy.JConomyExtension;

public class DefaultExtensionLoader implements ExtensionLoader {
    private final JavaPlugin plugin;
    private final ClassLoader classLoader;
    private final File extensionFolder;

    public DefaultExtensionLoader(JavaPlugin plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;

        this.extensionFolder = new File(plugin.getDataFolder(), "extensions");
        if (!extensionFolder.exists()) {
            extensionFolder.mkdirs();
        }
    }

    @Override
    public Set<LoadedExtension> load() {
        var jars = extensionFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null)
            return Set.of();

        var loadedExtensions = new HashSet<LoadedExtension>();

        for (File jar : jars) {
            try {
                var extensions = loadExtension(jar);
                loadedExtensions.addAll(extensions);
            } catch (Exception ex) {
                plugin.getLogger().warning(String.format("Failed to load extensions from '%s': %s", jar.getName(), ex.getMessage()));
            }
        }

        return loadedExtensions;
    }

    private Set<LoadedExtension> loadExtension(File jar) throws Exception {
        try (
            var jarFile = new JarFile(jar);
        ) {
            plugin.getLogger().info("Loading extension: " + jar.getName());

            var entries = jarFile.entries();

            var extensions = new HashSet<LoadedExtension>();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                var className = getClassName(entry);
                var urlClassLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, classLoader);
                var clazz = urlClassLoader.loadClass(className);

                if (isInstantiatableExtension(clazz)) {
                    try {
                        var extension = createExtension(clazz);
                        plugin.getLogger().info("Loaded extension: " + extension.getName());
                        extensions.add(new LoadedExtension(extension, urlClassLoader));
                    } catch (Exception ex) {
                        plugin.getLogger().warning(String.format("Failed to load extension from '%s': %s", clazz.getName()));
                    }
                }
            }

            return extensions;
        }
    }

    private static String getClassName(JarEntry entry) {
        var className = entry.getName()
                .replace('/', '.')
                .substring(0, entry.getName().length() - 6);
        return className;
    }

    private static boolean isInstantiatableExtension(Class<?> type) {
        return JConomyExtension.class.isAssignableFrom(type)
                && !type.isInterface()
                && !Modifier.isAbstract(type.getModifiers());
    }

    private static JConomyExtension createExtension(Class<?> type) throws Exception {
        return (JConomyExtension) type.getConstructor().newInstance();
    }
}
