package com.jellyrekt.jconomy.config;

public interface JConomyConfig {
    String getDefaultCurrency();

    NumberFormatterOptions getDefaultNumberFormatterOptions();

    CurrencyOptions getCurrencyOptions(String currencyName);

    public interface NumberFormatterOptions {
        GroupingOptions getGroupingOptions();

        FractionalOptions getFractionalOptions();

        public interface GroupingOptions {
            int getGroupSize();

            String getGroupSeparator();
        }

        public interface FractionalOptions {
            boolean isRoundingEnabled();

            int getPlaces();

            String getSeparator();
        }
    }

    public interface CurrencyOptions {
        String getDisplayNameSingular();

        String getDisplayNamePlural();

        String getSymbol();

        String getFormatString();

        NumberFormatterOptions getNumberFormatterOptions();
    }
}
