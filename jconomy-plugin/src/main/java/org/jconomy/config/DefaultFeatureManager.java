package org.jconomy.config;

public class DefaultFeatureManager implements FeatureManager {
    private final JConomyConfig config;

    public DefaultFeatureManager(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled(String featureName) {
        return config.getSection("features").getSection(featureName).getBoolean("enabled", false);
    }
}
