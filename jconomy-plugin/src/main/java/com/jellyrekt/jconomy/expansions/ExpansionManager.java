package com.jellyrekt.jconomy.expansions;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;

public interface ExpansionManager {

    void configureServices(JConomyServiceBuilder builder);

    void close();

}