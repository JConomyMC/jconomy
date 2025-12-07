package com.jellyrekt.jconomy;

import java.math.BigDecimal;

import com.jellyrekt.jconomy.config.JConomyConfig;

public class DefaultNumberFormatter implements NumberFormatter {
    private final JConomyConfig config;

    public DefaultNumberFormatter(JConomyConfig config) {
        this.config = config;
    }

    @Override
    public String format(BigDecimal number) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }
    
}
