package org.jconomy.impl.bootstrap;

import org.jconomy.dependencyinjection.JConomyServiceBuilder;

public final class JConomyImplRegistrar {

    private JConomyImplRegistrar() {
    }

    public static void registerServices(JConomyServiceBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("builder cannot be null");
        }
    }
}
