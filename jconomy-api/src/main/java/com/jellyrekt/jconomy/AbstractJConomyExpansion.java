package com.jellyrekt.jconomy;

import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceBuilder;
import com.jellyrekt.jconomy.dependencyinjection.JConomyServiceProvider;

public abstract class AbstractJConomyExpansion implements JConomyExpansion {
    public void configureServices(JConomyServiceBuilder builder) { }
    public void onServicesReady(JConomyServiceProvider provider) { }
}
