package org.jconomy;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.dependencyinjection.JConomyServiceProvider;

public abstract class AbstractJConomyExpansion implements JConomyExpansion {
    public void configureServices(JConomyServiceBuilder builder) { }
    public void onServicesReady(JConomyServiceProvider provider) { }
}
