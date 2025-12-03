package com.jellyrekt.storage.fileconfiguration;

import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;

public interface FileConfigurationProvider {
    FileConfiguration getFileConfiguration();

    void reload() throws IOException;

    void save() throws IOException;
}
