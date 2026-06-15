package org.jconomy.extensions;

import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import org.jconomy.JConomyExtension;
import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

class DefaultExtensionManagerTests {

    @Test
    void notifyServicesReady_calls_onServicesReady_on_each_extension() {
        var extension1 = mock(JConomyExtension.class);
        var extension2 = mock(JConomyExtension.class);
        var classLoader1 = mock(java.net.URLClassLoader.class);
        var classLoader2 = mock(java.net.URLClassLoader.class);

        ExtensionLoader loader = () -> Set.of(
                new LoadedExtension(extension1, classLoader1),
                new LoadedExtension(extension2, classLoader2));

        var manager = new DefaultExtensionManager(loader, Logger.getLogger("test"));
        var provider = mock(JConomyServiceProvider.class);

        manager.notifyServicesReady(provider);

        verify(extension1).onServicesReady(provider);
        verify(extension2).onServicesReady(provider);
    }

    @Test
    void configureServices_calls_configureServices_on_each_extension() {
        var extension1 = mock(JConomyExtension.class);
        var classLoader1 = mock(java.net.URLClassLoader.class);

        ExtensionLoader loader = () -> Set.of(new LoadedExtension(extension1, classLoader1));

        var manager = new DefaultExtensionManager(loader, Logger.getLogger("test"));
        var builder = mock(JConomyServiceBuilder.class);

        manager.configureServices(builder);

        verify(extension1).configureServices(builder);
    }
}
