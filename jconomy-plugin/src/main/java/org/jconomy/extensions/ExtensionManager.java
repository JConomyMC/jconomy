package org.jconomy.extensions;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public interface ExtensionManager {

    void configureServices(JConomyServiceBuilder builder);

    void notifyServicesReady(JConomyServiceProvider provider);

    void close();

}
