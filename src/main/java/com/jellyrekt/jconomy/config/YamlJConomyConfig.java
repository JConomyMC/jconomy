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
            return new YamlCurrencyOptions(
                getDefaultNumberFormatterOptions(),
                () -> section.getConfigurationSection(key)
            );
        }
        return null;
    }
    
    public class YamlNumberFormatterOptions extends ConfigurationSectionStorage implements NumberFormatterOptions {
        private final NumberFormatterOptions defaultNumberFormatterOptions;

        public YamlNumberFormatterOptions(ConfigurationSectionProvider configurationSectionProvider) {
            this(null, configurationSectionProvider);
        }

        public YamlNumberFormatterOptions(
            NumberFormatterOptions defaultNumberFormatterOptions,
            ConfigurationSectionProvider configurationSectionProvider
        ) {
            super(configurationSectionProvider);
            this.defaultNumberFormatterOptions = defaultNumberFormatterOptions;
        }

        @Override
        public GroupingOptions getGroupingOptions() {
            var key = "grouping";
            var section = getConfigurationSection();
            if (section.isSet(key)) {
                if (defaultNumberFormatterOptions != null) {
                    return new YamlGroupingOptions(
                        defaultNumberFormatterOptions.getGroupingOptions(),
                        () -> section.getConfigurationSection(key)
                    );
                }
                return new YamlGroupingOptions(() -> section.getConfigurationSection(key));
            }
            if (defaultNumberFormatterOptions != null) {
                return defaultNumberFormatterOptions.getGroupingOptions();
            }
            return null;
        }

        @Override
        public FractionalOptions getFractionalOptions() {
            var key = "fractional";
            var section = getConfigurationSection();
            if (section.isSet(key)) {
                if (defaultNumberFormatterOptions != null) {
                    return new YamlFractionalOptions(
                        defaultNumberFormatterOptions.getFractionalOptions(),
                        () -> section.getConfigurationSection(key)
                    );
                }
                return new YamlFractionalOptions(() -> section.getConfigurationSection(key));
            }
            if (defaultNumberFormatterOptions != null) {
                return defaultNumberFormatterOptions.getFractionalOptions();
            }
            return null;
        }

        public class YamlGroupingOptions extends ConfigurationSectionStorage implements GroupingOptions {
            private final GroupingOptions defaultGroupingOptions;

            public YamlGroupingOptions(ConfigurationSectionProvider configurationSectionProvider) {
                this(null, configurationSectionProvider);
            }

            public YamlGroupingOptions(
                GroupingOptions defaultGroupingOptions,
                ConfigurationSectionProvider configurationSectionProvider
            ) {
                super(configurationSectionProvider);
                this.defaultGroupingOptions = defaultGroupingOptions;
            }

            @Override
            public int getGroupSize() {
                return getConfigurationSection().getInt("group-size");
            }

            @Override
            public String getGroupSeparator() {
                var section = getConfigurationSection();
                var key = "separator";
                if (defaultGroupingOptions != null) {
                    return section.getString(key, defaultGroupingOptions.getGroupSeparator());
                }
                return section.getString("separator");
            }

        }

        public class YamlFractionalOptions extends ConfigurationSectionStorage implements FractionalOptions {
            private final FractionalOptions defaultFractionalOptions;

            public YamlFractionalOptions(ConfigurationSectionProvider configurationSectionProvider) {
                this(null, configurationSectionProvider);
            }

            public YamlFractionalOptions(
                FractionalOptions defaultFractionalOptions,
                ConfigurationSectionProvider configurationSectionProvider
            ) {
                super(configurationSectionProvider);
                this.defaultFractionalOptions = defaultFractionalOptions;
            }

            @Override
            public boolean isRoundingEnabled() {
                var section = getConfigurationSection();
                var key = "round";
                if (defaultFractionalOptions != null) {
                    return section.getBoolean(key, defaultFractionalOptions.isRoundingEnabled());
                }
                return section.getBoolean(key);
            }

            @Override
            public int getPlaces() {
                var section = getConfigurationSection();
                var key = "places";
                if (defaultFractionalOptions != null) {
                    return section.getInt(key, defaultFractionalOptions.getPlaces());
                }
                return section.getInt(key);
            }

            @Override
            public String getSeparator() {
                var section = getConfigurationSection();
                var key = "separator";
                if (defaultFractionalOptions != null) {
                    return section.getString(key, defaultFractionalOptions.getSeparator());
                }
                return section.getString(key);
            }

        }
    }

    public class YamlCurrencyOptions extends ConfigurationSectionStorage implements CurrencyOptions {
        private final NumberFormatterOptions defaultNumberFormatterOptions;

        public YamlCurrencyOptions(ConfigurationSectionProvider configurationSectionProvider) {
            this(null, configurationSectionProvider);
        }

        public YamlCurrencyOptions(
            NumberFormatterOptions defaultNumberFormatterOptions,
            ConfigurationSectionProvider configurationSectionProvider
        ) {
            super(configurationSectionProvider);
            this.defaultNumberFormatterOptions = defaultNumberFormatterOptions;
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
                return new YamlNumberFormatterOptions(defaultNumberFormatterOptions, () -> section.getConfigurationSection(key));
            }
            return defaultNumberFormatterOptions;
        }

    }
}
