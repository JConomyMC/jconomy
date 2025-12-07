package com.jellyrekt.jconomy;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        var options = getNumberFormatterOptions(currency);
        var groupingOptions = options.getGroupingOptions();
        var fractionalOptions = options.getFractionalOptions();

        number = getRounded(number.abs(), fractionalOptions);

        var numberStrings = number.toString().split("\\.");
        var integerPart = numberStrings[0];
        var fractionalPart = numberStrings.length > 1 ? numberStrings[1] : "";

        var formattedNumberBuilder = new StringBuilder();

        var groupingSeparator = groupingOptions.getGroupSeparator();

        for (int i = 0; i < integerPart.length(); i++) {
            if (shouldAddGroupingSymbol(integerPart.length(), i, groupingOptions)) {
                formattedNumberBuilder.append(groupingSeparator);
            }
            formattedNumberBuilder.append(integerPart.charAt(i));
        }

        if (fractionalOptions.getPlaces() <= 0) {
            return formattedNumberBuilder.toString();
        }

        formattedNumberBuilder.append(fractionalOptions.getSeparator());

        for (int i = 0; i < fractionalOptions.getPlaces(); i++) {
            if (i < fractionalPart.length()) {
                formattedNumberBuilder.append(fractionalPart.charAt(i));
            }
            else {
                formattedNumberBuilder.append(0);
            }
        }

        return formattedNumberBuilder.toString();
    }

    private NumberFormatterOptions getNumberFormatterOptions(String currency) {
        return config.getCurrencyOptions(currency).getNumberFormatterOptions();
    }

    private static BigDecimal getRounded(BigDecimal number, FractionalOptions fractionalOptions) {
        if (fractionalOptions.isRoundingEnabled()) {
            var places = Math.max(0, fractionalOptions.getPlaces());
            return number.setScale(places, RoundingMode.HALF_UP);
        }
        return number;
    }

    boolean shouldAddGroupingSymbol(int length, int charIndex, GroupingOptions options) {
        if (options.getGroupSize() <= 0) {
            return false;
        }
        if (charIndex == 0) {
            return false;
        }
        return (length - charIndex - 1) % options.getGroupSize() == 0;
    }

}
