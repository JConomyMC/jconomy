package org.jconomy.presentation;

import java.math.BigDecimal;

import org.jconomy.config.economy.EconomyConfig;

public class DefaultCurrencyFormatter implements CurrencyFormatter {
    private final EconomyConfig config;
    private final NumberFormatter numberFormatter;

    public DefaultCurrencyFormatter(EconomyConfig config, NumberFormatter numberFormatter) {
        this.config = config;
        this.numberFormatter = numberFormatter;
    }

    @Override
    public String format(BigDecimal amount, String currency) {
        var currencyOptions = config.getCurrencyOptions(currency);
        var formatResult = numberFormatter.format(amount, currency);

        return currencyOptions.getFormatString()
            .replaceAll("%sign%", formatResult.isNegative() ? "-" : "")
            .replaceAll("%symbol%", currencyOptions.getSymbol())
            .replaceAll("%amount_raw%", amount.abs().toString())
            .replaceAll("%amount_formatted%", formatResult.formattedNumber())
            .replaceAll("%display_name%", formatResult.isSingular() ? currencyOptions.getDisplayNameSingular() : currencyOptions.getDisplayNamePlural());
    }
}
