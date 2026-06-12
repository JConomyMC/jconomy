package com.jellyrekt.jconomy.expansions;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;

public interface ExpansionManager {

    void configureServices(JConomyServiceBuilder builder);

    void notifyServicesReady(JConomyServiceProvider provider);

    void close();

}