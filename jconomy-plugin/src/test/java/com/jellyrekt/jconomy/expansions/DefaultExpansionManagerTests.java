package com.jellyrekt.jconomy.expansions;

import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.JConomyExpansion;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;

class DefaultExpansionManagerTests {

    @Test
    void notifyServicesReady_calls_onServicesReady_on_each_expansion() {
        var expansion1 = mock(JConomyExpansion.class);
        var expansion2 = mock(JConomyExpansion.class);
        var classLoader1 = mock(java.net.URLClassLoader.class);
        var classLoader2 = mock(java.net.URLClassLoader.class);

        ExpansionLoader loader = () -> Set.of(
                new LoadedExpansion(expansion1, classLoader1),
                new LoadedExpansion(expansion2, classLoader2));

        var manager = new DefaultExpansionManager(loader, Logger.getLogger("test"));
        var provider = mock(JConomyServiceProvider.class);

        manager.notifyServicesReady(provider);

        verify(expansion1).onServicesReady(provider);
        verify(expansion2).onServicesReady(provider);
    }

    @Test
    void configureServices_calls_configureServices_on_each_expansion() {
        var expansion1 = mock(JConomyExpansion.class);
        var classLoader1 = mock(java.net.URLClassLoader.class);

        ExpansionLoader loader = () -> Set.of(new LoadedExpansion(expansion1, classLoader1));

        var manager = new DefaultExpansionManager(loader, Logger.getLogger("test"));
        var builder = mock(JConomyServiceBuilder.class);

        manager.configureServices(builder);

        verify(expansion1).configureServices(builder);
    }
}
