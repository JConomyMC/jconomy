package org.jconomy.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.config.economy.EconomyConfig.CurrencyOptions;

class DefaultCurrencyFormatterTests {

    private EconomyConfig config;
    private CurrencyOptions currencyOptions;
    private NumberFormatter numberFormatter;
    private DefaultCurrencyFormatter formatter;

    @BeforeEach
    void setUp() {
        config = mock(EconomyConfig.class);
        currencyOptions = mock(CurrencyOptions.class);
        numberFormatter = mock(NumberFormatter.class);
        formatter = new DefaultCurrencyFormatter(config, numberFormatter);
        when(config.getCurrencyOptions("gold")).thenReturn(currencyOptions);
    }

    @Test
    void format_substitutes_sign_with_empty_string_for_positive_amount() {
        when(currencyOptions.getFormatString()).thenReturn("%sign%100");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(new BigDecimal("100"), "gold"))
                .thenReturn(new NumberFormatResult("100", false, false));

        assertEquals("100", formatter.format(new BigDecimal("100"), "gold"));
    }

    @Test
    void format_substitutes_sign_with_dash_for_negative_amount() {
        when(currencyOptions.getFormatString()).thenReturn("%sign%%amount_raw%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(new BigDecimal("-50"), "gold"))
                .thenReturn(new NumberFormatResult("50", false, true));

        assertEquals("-50", formatter.format(new BigDecimal("-50"), "gold"));
    }

    @Test
    void format_substitutes_symbol_placeholder() {
        when(currencyOptions.getFormatString()).thenReturn("%symbol%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(BigDecimal.ONE, "gold"))
                .thenReturn(new NumberFormatResult("1", true, false));

        assertEquals("G", formatter.format(BigDecimal.ONE, "gold"));
    }

    @Test
    void format_substitutes_amount_raw_with_absolute_value() {
        when(currencyOptions.getFormatString()).thenReturn("%amount_raw%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(new BigDecimal("42.5"), "gold"))
                .thenReturn(new NumberFormatResult("42.5", false, false));

        assertEquals("42.5", formatter.format(new BigDecimal("42.5"), "gold"));
    }

    @Test
    void format_substitutes_amount_formatted_placeholder() {
        when(currencyOptions.getFormatString()).thenReturn("%amount_formatted%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(new BigDecimal("1000"), "gold"))
                .thenReturn(new NumberFormatResult("1,000", false, false));

        assertEquals("1,000", formatter.format(new BigDecimal("1000"), "gold"));
    }

    @Test
    void format_uses_singular_display_name_when_amount_is_one() {
        when(currencyOptions.getFormatString()).thenReturn("%display_name%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(BigDecimal.ONE, "gold"))
                .thenReturn(new NumberFormatResult("1", true, false));

        assertEquals("Gold", formatter.format(BigDecimal.ONE, "gold"));
    }

    @Test
    void format_uses_plural_display_name_when_amount_is_not_one() {
        when(currencyOptions.getFormatString()).thenReturn("%display_name%");
        when(currencyOptions.getSymbol()).thenReturn("G");
        when(currencyOptions.getDisplayNameSingular()).thenReturn("Gold");
        when(currencyOptions.getDisplayNamePlural()).thenReturn("Golds");
        when(numberFormatter.format(new BigDecimal("5"), "gold"))
                .thenReturn(new NumberFormatResult("5", false, false));

        assertEquals("Golds", formatter.format(new BigDecimal("5"), "gold"));
    }
}