package com.jellyrekt.jconomy.storage;

import java.io.File;
import java.io.IOException;

import com.jellyrekt.storage.fileconfiguration.yaml.YamlConfigurationProvider;

public class ConfigFileConfigurationProvider extends YamlConfigurationProvider {

    public ConfigFileConfigurationProvider(File parent, String fileName) throws IOException {
        super(parent, fileName);
    }
    
}
