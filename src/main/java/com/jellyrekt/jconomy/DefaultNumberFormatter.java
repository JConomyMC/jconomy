package com.jellyrekt.jconomy;

import java.math.BigDecimal;

import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions;

public class DefaultNumberFormatter implements NumberFormatter {
    private final JConomyConfig config;

    public DefaultNumberFormatter(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public String format(BigDecimal number, String currency) {
        // TODO: figure out how to get default options33
        var numberStrings = number.toString().split(".");
        var integerPart = numberStrings[0];
        var decimalPart = numberStrings.length > 1 ? numberStrings[2] : null;

        var formattedNumberBuilder = new StringBuilder();

        var options = getNumberFormatterOptions(currency);
        var groupingOptions = options.getGroupingOptions();
        var fractionalOptions = options.getFractionalOptions();

        var groupingSeparator = groupingOptions.getGroupSeparator();

        for (int i = 0; i < integerPart.length(); i++) {
            if (shouldAddGroupingSymbol(i)) {
                formattedNumberBuilder.append(groupingSeparator);
            }
            formattedNumberBuilder.append(integerPart.charAt(i));
        }

        var places = Math.max(0, Math.min(fractionalOptions.getPlaces(), decimalPart.length()));
        
        for (int i = 0; i < places - 1; i++) {

        }

        // TODO check rounding options to determine last char

        return formattedNumberBuilder.toString();
    }

    private NumberFormatterOptions getNumberFormatterOptions(String currency) {
        return config.getCurrencyOptions(currency).getNumberFormatterOptions();
    }

    boolean shouldAddGroupingSymbol(int i) {
        // TODO
        return false;
    }
    
    boolean shouldAddDecimal(int i) {
        // TODO
        return false;
    }


}
