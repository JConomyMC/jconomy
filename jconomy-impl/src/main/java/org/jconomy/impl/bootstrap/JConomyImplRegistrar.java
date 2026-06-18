package org.jconomy.impl.bootstrap;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;
import org.jconomy.storage.DefaultFlushRegistry;
import org.jconomy.storage.FlushRegistry;

public final class JConomyImplRegistrar {

    private JConomyImplRegistrar() {
    }

    public static void registerServices(JConomyServiceBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder cannot be null");
        }

        builder.addSingleton(FlushRegistry.class, DefaultFlushRegistry.class);
    }
}
