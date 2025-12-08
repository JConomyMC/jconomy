package com.jellyrekt.jconomy.presentation;

import java.math.BigDecimal;

import com.jellyrekt.jconomy.config.JConomyConfig;

public class DefaultCurrencyFormatter implements CurrencyFormatter {
    private final JConomyConfig config;
    private final NumberFormatter numberFormatter;

    public DefaultCurrencyFormatter(JConomyConfig config, NumberFormatter numberFormatter) {
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
