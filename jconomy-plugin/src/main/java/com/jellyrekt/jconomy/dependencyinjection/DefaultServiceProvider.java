package com.jellyrekt.jconomy.dependencyinjection;

import java.util.List;

import com.merenze.dependencyinjection.ServiceProvider;

public class DefaultServiceProvider implements JConomyServiceProvider {
    private ServiceProvider services;

    protected void setDelegate(ServiceProvider services) {
        this.services = services;
    }

    private void validateState() {
        if (services == null) {
            throw new IllegalStateException("Cannot resolve services before delegate is set");
        }
    }

    @Override
    public <T> T getService(Class<T> type) {
        if (type == JConomyServiceProvider.class) {
            return type.cast(this);
        }
        validateState();
        return services.getService(type);
    }

    @Override
    public <T> T getRequiredService(Class<T> type) {
        if (type == JConomyServiceProvider.class) {
            return type.cast(this);
        }
        validateState();
        return services.getRequiredService(type);
    }

    @Override
    public <T> List<T> getServices(Class<T> type) {
        if (type == JConomyServiceProvider.class) {
            return List.of(type.cast(this));
        }
        validateState();
        return services.getServices(type);
    }
    
}
