package org.jconomy.extensions;

import java.util.Set;
import java.util.logging.Logger;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public class DefaultExtensionManager implements ExtensionManager {
    private final Set<LoadedExtension> loadedExtensions;
    private final Logger logger;

    public DefaultExtensionManager(ExtensionLoader loader, Logger logger) {
        loadedExtensions = loader.load();
        this.logger = logger;
    }

    @Override
    public void configureServices(JConomyServiceBuilder builder) {
        loadedExtensions.forEach(extension -> extension.extension().configureServices(builder));
    }

    @Override
    public void notifyServicesReady(JConomyServiceProvider provider) {
        loadedExtensions.forEach(extension -> extension.extension().onServicesReady(provider));
    }

    @Override
    public void close() {
        loadedExtensions.forEach(extension -> {
            try {
                extension.classLoader().close();
            } catch (Exception ex) {
                logger.warning(String.format("Failed to close classloader for extension '%s': %s", extension.extension().getName(),
                        ex.getMessage()));
            }
        });
    }
}
