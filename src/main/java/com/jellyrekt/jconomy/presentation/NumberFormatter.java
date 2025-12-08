package com.jellyrekt.jconomy.presentation;

import java.math.BigDecimal;

public interface NumberFormatter {
    NumberFormatResult format(BigDecimal number, String currency);
}
