package com.jellyrekt.jconomy;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;

public interface JConomyExpansion {
    String getName();
    void configureServices(JConomyServiceBuilder builder);
}
