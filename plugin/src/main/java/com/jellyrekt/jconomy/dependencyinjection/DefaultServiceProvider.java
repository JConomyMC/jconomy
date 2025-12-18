package com.jellyrekt.jconomy.dependencyinjection;

import java.util.List;

import com.merenze.dependencyinjection.ServiceProvider;

public class DefaultServiceProvider implements JConomyServiceProvider {
    private final ServiceProvider services;

    public DefaultServiceProvider(ServiceProvider services) {
        this.services = services;
    }

    @Override
    public <T> T getService(Class<T> type) {
        return services.getService(type);
    }

    @Override
    public <T> T getRequiredService(Class<T> type) {
        return services.getRequiredService(type);
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        return services.getServices(type);
    }
    
}
