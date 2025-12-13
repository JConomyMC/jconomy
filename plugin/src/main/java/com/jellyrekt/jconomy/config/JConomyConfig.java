package com.jellyrekt.jconomy.config;

import java.util.Set;

public interface JConomyConfig {
    String getDefaultCurrency();

    String getDefaultWorldName();

    NumberFormatterOptions getDefaultNumberFormatterOptions();

    Set<String> getAllCurrencyNames();

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
