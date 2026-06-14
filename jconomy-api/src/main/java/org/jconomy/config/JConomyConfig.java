package org.jconomy.config;

import java.util.List;
import java.util.Set;

public interface JConomyConfig {
    boolean getBoolean(String path);

    boolean getBoolean(String path, boolean def);

    List<Boolean> getBooleanList(String path);

    double getDouble(String path);

    double getDouble(String path, double def);

    List<Double> getDoubleList(String path);

    int getInt(String path);

    int getInt(String path, int def);

    List<Integer> getIntegerList(String path);

    String getString(String path);

    String getString(String path, String def);

    List<String> getStringList(String path);

    boolean contains(String path);

    boolean isSet(String path);

    Set<String> getKeys(boolean deep);
    
    JConomyConfig getSection(String path);
}
