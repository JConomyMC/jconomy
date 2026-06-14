package org.jconomy.config;

public interface FeatureManager {
    String DATA_TRANSFER = "data-transfer";

    boolean isEnabled(String featureName);
}
