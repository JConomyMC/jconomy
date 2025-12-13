package com.jellyrekt.jconomy.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.config.JConomyConfig.CurrencyOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.FractionalOptions;
import com.jellyrekt.jconomy.config.JConomyConfig.NumberFormatterOptions.GroupingOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultNumberFormatterTests {

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

    record TestCase(
            BigDecimal input,
            int groupSize,
            String groupSeparator,
            int decimalPlaces,
            boolean rounding,
            String decimalSeparator,
            String expectedFormatted,
            boolean expectedSingular,
            boolean expectedNegative) {
    }

    static Stream<TestCase> numberFormattingCases() {
        return Stream.of(
                new TestCase(BigDecimal.valueOf(1234.56), 0, ",", 0, true, ".", "1235", false, false),
                new TestCase(BigDecimal.valueOf(-1234.56), 0, ",", 0, true, ".", "1235", false, true),
                new TestCase(BigDecimal.valueOf(12.3), 0, ",", 4, false, ".", "12.3000", false, false),
                new TestCase(BigDecimal.valueOf(-5.6), 0, ",", 3, false, ".", "5.600", false, true),
                new TestCase(BigDecimal.valueOf(1234567.891), 3, ",", 2, true, ".", "1,234,567.89", false, false),
                new TestCase(BigDecimal.valueOf(-98765.4321), 3, ",", 1, true, ".", "98,765.4", false, true),
                new TestCase(BigDecimal.ZERO, 3, ",", 2, true, ".", "0.00", false, false),
                new TestCase(BigDecimal.valueOf(1.5), 0, ",", 0, true, ".", "2", false, false),
                new TestCase(BigDecimal.valueOf(1.5), 0, ",", 0, false, ".", "1", true, false),
                new TestCase(BigDecimal.valueOf(-1.5), 0, ",", 0, true, ".", "2", false, true),
                new TestCase(BigDecimal.valueOf(-1.5), 0, ",", 0, false, ".", "1", true, true),
                new TestCase(BigDecimal.valueOf(1.4), 0, ",", 0, true, ".", "1", true, false));
    }

    @ParameterizedTest
    @MethodSource("numberFormattingCases")
    void testNumberFormatting(TestCase tc) {
        when(mockGrouping.getGroupSize()).thenReturn(tc.groupSize);
        when(mockGrouping.getGroupSeparator()).thenReturn(tc.groupSeparator);
        when(mockFractional.getPlaces()).thenReturn(tc.decimalPlaces);
        when(mockFractional.isRoundingEnabled()).thenReturn(tc.rounding);
        when(mockFractional.getSeparator()).thenReturn(tc.decimalSeparator);

        NumberFormatResult result = formatter.format(tc.input, "gold");

        assertEquals(tc.expectedFormatted, result.formattedNumber(), "Formatted string mismatch");
        assertEquals(tc.expectedSingular, result.isSingular(), "Singular flag mismatch");
        assertEquals(tc.expectedNegative, result.isNegative(), "Negative flag mismatch");
    }
}
