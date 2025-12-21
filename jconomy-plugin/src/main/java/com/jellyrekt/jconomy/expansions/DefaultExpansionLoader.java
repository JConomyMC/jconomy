package com.jellyrekt.jconomy.expansions;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.JConomyExpansion;

public class DefaultExpansionLoader implements ExpansionLoader {
    private final JavaPlugin plugin;
    private final ClassLoader classLoader;
    private final File moduleFolder;

    public DefaultExpansionLoader(JavaPlugin plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;

        this.moduleFolder = new File(plugin.getDataFolder(), "modules");
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }
    }

    @Override
    public Set<LoadedExpansion> load() {
        var jars = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null)
            return Set.of();

        var loadedModules = new HashSet<LoadedExpansion>();

        for (File jar : jars) {
            try {
                var expansions = loadExpansion(jar);
                loadedModules.addAll(expansions);
            } catch (Exception ex) {
                plugin.getLogger().warning(String.format("Failed to load expansions from '%s': %s", jar.getName(), ex.getMessage()));
            }
        }

        return loadedModules;
    }

    private Set<LoadedExpansion> loadExpansion(File jar) throws Exception {
        try (
            var jarFile = new JarFile(jar);
        ) {
            plugin.getLogger().info("Loading module: " + jar.getName());

            var entries = jarFile.entries();

            var expansions = new HashSet<LoadedExpansion>();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                var className = getClassName(entry);
                var urlClassLoader = new URLClassLoader(new URL[] { jar.toURI().toURL() }, classLoader);
                var clazz = urlClassLoader.loadClass(className);

                if (isInstantiatableExpansion(clazz)) {
                    try {
                        var expansion = createModule(clazz);
                        plugin.getLogger().info("Loaded expansion: " + expansion.getName());
                        expansions.add(new LoadedExpansion(expansion, urlClassLoader));
                    } catch (Exception ex) {
                        plugin.getLogger().warning(String.format("Failed to load expansion from '%s': %s", clazz.getName()));
                    }
                }
            }

            return expansions;
        }
    }

    private static String getClassName(JarEntry entry) {
        var className = entry.getName()
                .replace('/', '.')
                .substring(0, entry.getName().length() - 6);
        return className;
    }
    
    private static boolean isInstantiatableExpansion(Class<?> type) {
        return JConomyExpansion.class.isAssignableFrom(type)
                && !type.isInterface()
                && !Modifier.isAbstract(type.getModifiers());
    }
    
    private static JConomyExpansion createModule(Class<?> type) throws Exception {
        return (JConomyExpansion) type.getConstructor().newInstance();
    }
}
