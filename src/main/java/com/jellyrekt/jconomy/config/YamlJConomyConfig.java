package com.jellyrekt.jconomy.config;

import com.jellyrekt.storage.configurationsection.ConfigurationSectionProvider;
import com.jellyrekt.storage.configurationsection.ConfigurationSectionStorage;
import com.jellyrekt.storage.fileconfiguration.FileConfigurationStorage;
import com.jellyrekt.storage.fileconfiguration.javaplugin.JavaPluginConfigurationProvider;

public class YamlJConomyConfig extends FileConfigurationStorage implements JConomyConfig {

    public YamlJConomyConfig(JavaPluginConfigurationProvider configurationProvider) {
        super(configurationProvider);
    }

    @Override
    public String getDefaultCurrency() {
        return getFileConfiguration().getString("default-currency");
    }

    @Override
    public NumberFormatterOptions getDefaultNumberFormatterOptions() {
        var key = "default-number-formatter-options";
        var section = getFileConfiguration();
        if (section.isSet(key)) {
            return new YamlNumberFormatterOptions(() -> section.getConfigurationSection(key));
        }
        return null;
    }

    @Override
    public CurrencyOptions getCurrencyOptions(String currencyName) {
        var key = "currencies." + currencyName;
        var section = getFileConfiguration();
        if (section.isSet(key)) {
            return new YamlCurrencyOptions(() -> section.getConfigurationSection(key));
        }
        return null;
    }
    
    public class YamlNumberFormatterOptions extends ConfigurationSectionStorage implements NumberFormatterOptions {

        public YamlNumberFormatterOptions(ConfigurationSectionProvider configurationSectionProvider) {
            super(configurationSectionProvider);
        }

        @Override
        public GroupingOptions getGroupingOptions() {
            var key = "grouping";
            var section = getConfigurationSection();
            if (section.isSet(key)) {
                return new YamlGroupingOptions(() -> section.getConfigurationSection("key"));
            }
            return null;
        }

        @Override
        public FractionalOptions getFractionalOptions() {
            var key = "fractional";
            var section = getConfigurationSection();
            if (section.isSet(key)) {
                return new YamlFractionalOptions(() -> section.getConfigurationSection(key));
            }
            return null;
        }

        public class YamlGroupingOptions extends ConfigurationSectionStorage implements GroupingOptions {

            public YamlGroupingOptions(ConfigurationSectionProvider configurationSectionProvider) {
                super(configurationSectionProvider);
            }

            @Override
            public int getGroupSize() {
                return getConfigurationSection().getInt("group-size");
            }

            @Override
            public String getGroupSeparator() {
                return getConfigurationSection().getString("separator");
            }

        }

        public class YamlFractionalOptions extends ConfigurationSectionStorage implements FractionalOptions {

            public YamlFractionalOptions(ConfigurationSectionProvider configurationSectionProvider) {
                super(configurationSectionProvider);
            }

            @Override
            public boolean isRoundingEnabled() {
                return getConfigurationSection().getBoolean("round");
            }

            @Override
            public int getPlaces() {
                return getConfigurationSection().getInt("places");
            }

            @Override
            public String getSeparator() {
                return getConfigurationSection().getString("separator");
            }

        }
    }

    public class YamlCurrencyOptions extends ConfigurationSectionStorage implements CurrencyOptions {

        public YamlCurrencyOptions(ConfigurationSectionProvider configurationSectionProvider) {
            super(configurationSectionProvider);
        }

        @Override
        public String getDisplayNameSingular() {
            return getConfigurationSection().getString(
                "display-name-singular",
                getConfigurationSection().getString("display-name-plural")
            );
        }

        @Override
        public String getDisplayNamePlural() {
            return getConfigurationSection().getString(
                "display-name-plural",
                getConfigurationSection().getString("display-name-singular")
            );
        }

        @Override
        public String getSymbol() {
            return getConfigurationSection().getString("symbol");
        }

        @Override
        public String getFormatString() {
            return getConfigurationSection().getString("format-string");
        }

        @Override
        public NumberFormatterOptions getNumberFormatterOptions() {
            var key = "number-formatter";
            var section = getConfigurationSection();
            if (section.isSet(key)) {
                return new YamlNumberFormatterOptions(() -> section.getConfigurationSection(key));
            }
            return null;
        }

    }
}
