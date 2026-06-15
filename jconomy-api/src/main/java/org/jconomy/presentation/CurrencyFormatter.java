package org.jconomy.presentation;

import java.math.BigDecimal;

public interface CurrencyFormatter {
    String format(BigDecimal amount, String currency);
}
