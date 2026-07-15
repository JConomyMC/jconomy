package org.jconomy.extensions;

import org.jconomy.dependencyinjection.JConomyServiceRegistrar;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public interface ExtensionManager {

    void configureServices(JConomyServiceRegistrar registrar);

    void notifyServicesReady(JConomyServiceProvider provider);

    void close();

}
