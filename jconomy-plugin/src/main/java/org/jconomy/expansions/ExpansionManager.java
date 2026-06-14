package org.jconomy.expansions;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public interface ExpansionManager {

    void configureServices(JConomyServiceBuilder builder);

    void notifyServicesReady(JConomyServiceProvider provider);

    void close();

}