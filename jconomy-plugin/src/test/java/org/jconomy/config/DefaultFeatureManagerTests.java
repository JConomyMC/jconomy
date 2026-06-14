package org.jconomy.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.jconomy.FeatureManager;
import org.jconomy.FeatureNames;

class DefaultFeatureManagerTests {

    @Test
    void isEnabled_returns_false_when_features_section_absent() {
        var featuresSection = mock(JConomyConfig.class);
        var featureSection = mock(JConomyConfig.class);
        when(featureSection.getBoolean("enabled", false)).thenReturn(false);
        when(featuresSection.getSection("data-transfer")).thenReturn(featureSection);

        var root = mock(JConomyConfig.class);
        when(root.getSection("features")).thenReturn(featuresSection);

        var manager = new DefaultFeatureManager(root);

        assertFalse(manager.isEnabled(FeatureNames.DATA_TRANSFER));
    }

    @Test
    void isEnabled_returns_false_when_feature_disabled() {
        var featureSection = mock(JConomyConfig.class);
        when(featureSection.getBoolean("enabled", false)).thenReturn(false);

        var featuresSection = mock(JConomyConfig.class);
        when(featuresSection.getSection("data-transfer")).thenReturn(featureSection);

        var root = mock(JConomyConfig.class);
        when(root.getSection("features")).thenReturn(featuresSection);

        var manager = new DefaultFeatureManager(root);

        assertFalse(manager.isEnabled(FeatureNames.DATA_TRANSFER));
    }

    @Test
    void isEnabled_returns_true_when_feature_enabled() {
        var featureSection = mock(JConomyConfig.class);
        when(featureSection.getBoolean("enabled", false)).thenReturn(true);

        var featuresSection = mock(JConomyConfig.class);
        when(featuresSection.getSection("data-transfer")).thenReturn(featureSection);

        var root = mock(JConomyConfig.class);
        when(root.getSection("features")).thenReturn(featuresSection);

        var manager = new DefaultFeatureManager(root);

        assertTrue(manager.isEnabled(FeatureNames.DATA_TRANSFER));
    }
}
