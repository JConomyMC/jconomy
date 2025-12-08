package com.jellyrekt.jconomy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.FractionalOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.GroupingOptions;

public class DefaultNumberFormatter implements NumberFormatter {
    private final JConomyConfig config;

    public DefaultNumberFormatter(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public String format(BigDecimal number, String currency) {
        var decimalFormat = getDecimalFormat(currency);
        number = getRounded(number, currency).abs();
        return decimalFormat.format(number.abs());
    }

    private BigDecimal getRounded(BigDecimal number, String currency) {
        var fractionalOptions = getNumberFormatterOptions(currency).getFractionalOptions();
        int places = Math.max(0, fractionalOptions.getPlaces());

        if (fractionalOptions.isRoundingEnabled()) {
            return number.setScale(places, RoundingMode.HALF_UP);
        } else {
            return number.setScale(places, RoundingMode.DOWN);
        }
    }

    private NumberFormatterOptions getNumberFormatterOptions(String currency) {
        return config.getCurrencyOptions(currency).getNumberFormatterOptions();
    }

    private DecimalFormat getDecimalFormat(String currency) {
        var options = getNumberFormatterOptions(currency);
        var groupingOptions = options.getGroupingOptions();
        var fractionalOptions = options.getFractionalOptions();

        var formatSymbols = getDecimalFormatSymbols(groupingOptions, fractionalOptions);
        var places = fractionalOptions.getPlaces();

        var decimalFormat = new DecimalFormat();

        decimalFormat.setDecimalFormatSymbols(formatSymbols);
        decimalFormat.setGroupingUsed(!groupingOptions.getGroupSeparator().isEmpty() && groupingOptions.getGroupSize() > 0);
        decimalFormat.setGroupingSize(groupingOptions.getGroupSize());
        decimalFormat.setMinimumFractionDigits(places);
        decimalFormat.setMaximumFractionDigits(places);

        return decimalFormat;
    }

    private DecimalFormatSymbols getDecimalFormatSymbols(GroupingOptions groupingOptions, FractionalOptions fractionalOptions) {
        var symbols = new DecimalFormatSymbols();

        var groupingSeparator = groupingOptions.getGroupSeparator();
        if (groupingSeparator != null && !groupingSeparator.isEmpty()) {
            symbols.setGroupingSeparator(groupingSeparator.charAt(0));
        }

        var decimalSeparator = fractionalOptions.getSeparator();
        if (decimalSeparator != null && !decimalSeparator.isEmpty()) {
            symbols.setDecimalSeparator(decimalSeparator.charAt(0));
        }

        return symbols;
    }

}
