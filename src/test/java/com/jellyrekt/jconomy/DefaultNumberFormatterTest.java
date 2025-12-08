package com.jellyrekt.jconomy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig.CurrencyOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.FractionalOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.GroupingOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultNumberFormatterTest {

    private JConomyConfig mockConfig;
    private NumberFormatterOptions mockNumberFormatterOptions;
    private GroupingOptions mockGrouping;
    private FractionalOptions mockFractional;
    private DefaultNumberFormatter formatter;
    private CurrencyOptions mockCurrencyOptions;

    @BeforeEach
    void setup() {
        mockConfig = mock(JConomyConfig.class);
        mockNumberFormatterOptions = mock(NumberFormatterOptions.class);
        mockGrouping = mock(GroupingOptions.class);
        mockFractional = mock(FractionalOptions.class);
        mockCurrencyOptions = mock(CurrencyOptions.class);

        when(mockNumberFormatterOptions.getGroupingOptions()).thenReturn(mockGrouping);
        when(mockNumberFormatterOptions.getFractionalOptions()).thenReturn(mockFractional);

        when(mockCurrencyOptions.getNumberFormatterOptions()).thenReturn(mockNumberFormatterOptions);

        when(mockConfig.getCurrencyOptions(anyString())).thenReturn(mockCurrencyOptions);

        formatter = new DefaultNumberFormatter(mockConfig);
    }

    @Test
    void testPositiveNumberNoGroupingNoDecimal() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockFractional.getPlaces()).thenReturn(0);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");
        when(mockGrouping.getGroupSeparator()).thenReturn(",");

        String result = formatter.format(BigDecimal.valueOf(1234.56), "gold");
        assertEquals("1235", result); // Rounds 1234.56 -> 1235
    }

    @Test
    void testNegativeNumberNoGroupingNoDecimal() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockFractional.getPlaces()).thenReturn(0);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");
        when(mockGrouping.getGroupSeparator()).thenReturn(",");

        String result = formatter.format(BigDecimal.valueOf(-1234.56), "gold");
        assertEquals("1235", result); // Absolute value rounded
    }

    @Test
    void testPositiveNumberWithDecimalPadding() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockFractional.getPlaces()).thenReturn(4);
        when(mockFractional.isRoundingEnabled()).thenReturn(false);
        when(mockFractional.getSeparator()).thenReturn(".");
        when(mockGrouping.getGroupSeparator()).thenReturn(",");

        String result = formatter.format(BigDecimal.valueOf(12.3), "gold");
        assertEquals("12.3000", result); // Pads zeros to 4 decimal places
    }

    @Test
    void testNegativeNumberWithDecimalPadding() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockFractional.getPlaces()).thenReturn(3);
        when(mockFractional.isRoundingEnabled()).thenReturn(false);
        when(mockFractional.getSeparator()).thenReturn(".");
        when(mockGrouping.getGroupSeparator()).thenReturn(",");

        String result = formatter.format(BigDecimal.valueOf(-5.6), "gold");
        assertEquals("5.600", result); // Pads zeros
    }

    @Test
    void testPositiveNumberWithGrouping() {
        when(mockGrouping.getGroupSize()).thenReturn(3);
        when(mockGrouping.getGroupSeparator()).thenReturn(",");
        when(mockFractional.getPlaces()).thenReturn(2);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");

        String result = formatter.format(BigDecimal.valueOf(1234567.891), "gold");
        assertEquals("1,234,567.89", result); // Rounds down after 2 decimals
    }

    @Test
    void testNegativeNumberWithGrouping() {
        when(mockGrouping.getGroupSize()).thenReturn(3);
        when(mockGrouping.getGroupSeparator()).thenReturn(",");
        when(mockFractional.getPlaces()).thenReturn(1);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");

        String result = formatter.format(BigDecimal.valueOf(-98765.4321), "gold");
        assertEquals("98,765.4", result);
    }

    @Test
    void testZeroNumber() {
        when(mockGrouping.getGroupSize()).thenReturn(3);
        when(mockGrouping.getGroupSeparator()).thenReturn(",");
        when(mockFractional.getPlaces()).thenReturn(2);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");

        String result = formatter.format(BigDecimal.ZERO, "gold");
        assertEquals("0.00", result);
    }

    @Test
    void testRoundingUp() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockGrouping.getGroupSeparator()).thenReturn(",");
        when(mockFractional.getPlaces()).thenReturn(0);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");

        String result = formatter.format(BigDecimal.valueOf(1.5), "gold");
        assertEquals("2", result);
    }

    @Test
    void testRoundingDown() {
        when(mockGrouping.getGroupSize()).thenReturn(0);
        when(mockGrouping.getGroupSeparator()).thenReturn(",");
        when(mockFractional.getPlaces()).thenReturn(0);
        when(mockFractional.isRoundingEnabled()).thenReturn(true);
        when(mockFractional.getSeparator()).thenReturn(".");

        String result = formatter.format(BigDecimal.valueOf(1.4), "gold");
        assertEquals("1", result);
    }
}
