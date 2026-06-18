package org.jconomy.config.economy;

import java.util.Set;

public interface EconomyConfig {
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

        CurrencyCacheOptions getCacheOptions();

        public interface CurrencyCacheOptions {
            boolean isWarmOnJoinEnabled();

            boolean isWarmOnTeleportEnabled();
        }
    }
}
