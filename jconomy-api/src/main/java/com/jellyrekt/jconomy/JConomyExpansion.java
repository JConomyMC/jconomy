package com.jellyrekt.jconomy;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;

public interface JConomyExpansion {
    String getName();
    void configureServices(JConomyServiceBuilder builder);
    void onServicesReady(JConomyServiceProvider provider);
}
