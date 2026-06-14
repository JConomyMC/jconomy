package org.jconomy.config;

import java.util.List;
import java.util.Set;

import com.jellyrekt.storage.configuration.ConfigurationSectionProvider;
import com.jellyrekt.storage.configuration.ConfigurationSectionStorage;

public class DefaultJConomyConfig extends ConfigurationSectionStorage implements JConomyConfig {

    public DefaultJConomyConfig(ConfigurationSectionProvider configurationSectionProvider) {
        super(configurationSectionProvider);
    }

    @Override
    public boolean getBoolean(String path) {
        return getConfigurationSection().getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return getConfigurationSection().getBoolean(path, def);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return getConfigurationSection().getBooleanList(path);
    }

    @Override
    public double getDouble(String path) {
        return getConfigurationSection().getDouble(path);
    }

    @Override
    public double getDouble(String path, double def) {
        return getConfigurationSection().getDouble(path, def);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return getConfigurationSection().getDoubleList(path);
    }

    @Override
    public int getInt(String path) {
        return getConfigurationSection().getInt(path);
    }

    @Override
    public int getInt(String path, int def) {
        return getConfigurationSection().getInt(path, def);
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        return getConfigurationSection().getIntegerList(path);
    }

    @Override
    public String getString(String path) {
        return getConfigurationSection().getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return getConfigurationSection().getString(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return getConfigurationSection().getStringList(path);
    }

    @Override
    public boolean contains(String path) {
        return getConfigurationSection().contains(path);
    }

    @Override
    public boolean isSet(String path) {
        return getConfigurationSection().isSet(path);
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        return getConfigurationSection().getKeys(deep);
    }

    @Override
    public JConomyConfig getSection(String path) {
        var section = getConfigurationSection();
        return new DefaultJConomyConfig(() -> section.getConfigurationSection(path));
    }

}
