package com.jellyrekt.configuration.migration;

import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigMigrator {

    private final String versionPath;
    private final List<RegisteredConfigMigration> migrations;

    private ConfigMigrator(String versionPath, List<RegisteredConfigMigration> migrations) {
        this.versionPath = versionPath;
        this.migrations = migrations;
    }

    public void migrate(Configuration config) {
        int currentVersion = config.getInt(versionPath, 0);
        for (RegisteredConfigMigration registered : migrations) {
            if (currentVersion < registered.targetVersion()) {
                registered.migration().apply(config);
                config.set(versionPath, registered.targetVersion());
                currentVersion = registered.targetVersion();
            }
        }
    }

    public static Builder builder(String versionPath) {
        return new Builder(versionPath);
    }

    private record RegisteredConfigMigration(int targetVersion, ConfigMigration migration) {}

    public static class Builder {

        private final String versionPath;
        private final List<ConfigMigration> migrations = new ArrayList<>();

        private Builder(String versionPath) {
            this.versionPath = versionPath;
        }

        public Builder addNext(ConfigMigration migration) {
            migrations.add(migration);
            return this;
        }

        public ConfigMigrator build() {
            var registered = new ArrayList<RegisteredConfigMigration>();
            for (int i = 0; i < migrations.size(); i++) {
                registered.add(new RegisteredConfigMigration(i + 1, migrations.get(i)));
            }
            return new ConfigMigrator(versionPath, Collections.unmodifiableList(registered));
        }
    }
}
