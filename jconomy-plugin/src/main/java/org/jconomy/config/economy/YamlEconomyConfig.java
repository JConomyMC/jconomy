package org.jconomy.config.economy;

import java.util.Set;

import org.jconomy.config.JConomyConfig;

public class YamlEconomyConfig implements EconomyConfig {
    private final JConomyConfig config;

    public YamlEconomyConfig(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public String getDefaultCurrency() {
        return config.getString("default-currency");
    }

    @Override
    public String getDefaultWorldName() {
        return config.getString("default-world-name", "world");
    }

    @Override
    public NumberFormatterOptions getDefaultNumberFormatterOptions() {
        var key = "default-number-formatter-options";
        if (config.isSet(key)) {
            return new YamlNumberFormatterOptions(config.getSection(key));
        }
        return null;
    }
    
    @Override
    public Set<String> getAllCurrencyNames() {
        return config.getSection("currencies").getKeys(false);
    }

    @Override
    public CurrencyOptions getCurrencyOptions(String currencyName) {
        var key = "currencies." + currencyName;
        if (config.isSet(key)) {
            return new YamlCurrencyOptions(config.getSection(key), getDefaultNumberFormatterOptions());
        }
        return null;
    }
    
    public class YamlNumberFormatterOptions implements NumberFormatterOptions {
        private final JConomyConfig config;
        private final NumberFormatterOptions defaultNumberFormatterOptions;

        public YamlNumberFormatterOptions(JConomyConfig config) {
            this(config, null);
        }

        public YamlNumberFormatterOptions(
            JConomyConfig config,
            NumberFormatterOptions defaultNumberFormatterOptions
        ) {
            this.config = config;
            this.defaultNumberFormatterOptions = defaultNumberFormatterOptions;
        }

        @Override
        public GroupingOptions getGroupingOptions() {
            var key = "grouping";
            if (config.isSet(key)) {
                if (defaultNumberFormatterOptions != null) {
                    return new YamlGroupingOptions(config.getSection(key), defaultNumberFormatterOptions.getGroupingOptions());
                }
                return new YamlGroupingOptions(config.getSection(key));
            }
            if (defaultNumberFormatterOptions != null) {
                return defaultNumberFormatterOptions.getGroupingOptions();
            }
            return null;
        }

        @Override
        public FractionalOptions getFractionalOptions() {
            var key = "fractional";
            if (config.isSet(key)) {
                if (defaultNumberFormatterOptions != null) {
                    return new YamlFractionalOptions(config.getSection(key), defaultNumberFormatterOptions.getFractionalOptions());
                }
                return new YamlFractionalOptions(config.getSection(key));
            }
            if (defaultNumberFormatterOptions != null) {
                return defaultNumberFormatterOptions.getFractionalOptions();
            }
            return null;
        }

        public class YamlGroupingOptions implements GroupingOptions {
            private final JConomyConfig config;
            private final GroupingOptions defaultGroupingOptions;

            public YamlGroupingOptions(JConomyConfig config) {
                this(config, null);
            }

            public YamlGroupingOptions(JConomyConfig config, GroupingOptions defaultGroupingOptions) {
                this.config = config;
                this.defaultGroupingOptions = defaultGroupingOptions;
            }

            @Override
            public int getGroupSize() {
                var key = "group-size";
                if (defaultGroupingOptions != null) {
                    return config.getInt(key, defaultGroupingOptions.getGroupSize());
                }
                return config.getInt(key);
            }

            @Override
            public String getGroupSeparator() {
                var key = "separator";
                if (defaultGroupingOptions != null) {
                    return config.getString(key, defaultGroupingOptions.getGroupSeparator());
                }
                return config.getString(key);
            }

        }

        public class YamlFractionalOptions implements FractionalOptions {
            private final JConomyConfig config;
            private final FractionalOptions defaultFractionalOptions;

            public YamlFractionalOptions(JConomyConfig config) {
                this(config, null);
            }

            public YamlFractionalOptions(JConomyConfig config, FractionalOptions defaultFractionalOptions) {
                this.config = config;
                this.defaultFractionalOptions = defaultFractionalOptions;
            }

            @Override
            public boolean isRoundingEnabled() {
                var key = "round";
                if (defaultFractionalOptions != null) {
                    return config.getBoolean(key, defaultFractionalOptions.isRoundingEnabled());
                }
                return config.getBoolean(key);
            }

            @Override
            public int getPlaces() {
                var key = "places";
                if (defaultFractionalOptions != null) {
                    return config.getInt(key, defaultFractionalOptions.getPlaces());
                }
                return config.getInt(key);
            }

            @Override
            public String getSeparator() {
                var key = "separator";
                if (defaultFractionalOptions != null) {
                    return config.getString(key, defaultFractionalOptions.getSeparator());
                }
                return config.getString(key);
            }

        }
    }

    public class YamlCurrencyOptions implements CurrencyOptions {
        private final JConomyConfig config;
        private final NumberFormatterOptions defaultNumberFormatterOptions;

        public YamlCurrencyOptions(JConomyConfig config) {
            this(config, null);
        }

        public YamlCurrencyOptions(JConomyConfig config, NumberFormatterOptions defaultNumberFormatterOptions)
        {
            this.config = config;
            this.defaultNumberFormatterOptions = defaultNumberFormatterOptions;
        }

        @Override
        public String getDisplayNameSingular() {
            return config.getString(
                "display-name-singular",
                config.getString("display-name-plural")
            );
        }

        @Override
        public String getDisplayNamePlural() {
            return config.getString(
                "display-name-plural",
                config.getString("display-name-singular")
            );
        }

        @Override
        public String getSymbol() {
            return config.getString("symbol");
        }

        @Override
        public String getFormatString() {
            return config.getString("format-string");
        }

        @Override
        public NumberFormatterOptions getNumberFormatterOptions() {
            var key = "number-formatter";
            if (config.isSet(key)) {
                return new YamlNumberFormatterOptions(config.getSection(key), defaultNumberFormatterOptions);
            }
            return defaultNumberFormatterOptions;
        }

    }
}
