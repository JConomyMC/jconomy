package org.jconomy.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.jconomy.config.economy.YamlEconomyConfig;
import com.jellyrekt.storage.configuration.file.yaml.StringYamlConfigurationProvider;

public class YamlEconomyConfigTests {

  private static final String TEST_CURRENCY = "testcur";

  @ParameterizedTest(name = "{0}")
  @MethodSource("groupingAndFractionalCases")
  void groupingAndFractional_matrix(NumberFormatterCase c) {
    var config = getStringYamlJConomyConfig(c.yaml());
    var currency = config.getCurrencyOptions(TEST_CURRENCY);

    assertNotNull(currency, "Expected currency node to exist");

    var fmt = currency.getNumberFormatterOptions();

    if (!c.expectFormatterPresent()) {
      assertNull(fmt, "Expected formatter to be null");
      return;
    }

    assertNotNull(fmt);

    var g = fmt.getGroupingOptions();
    var f = fmt.getFractionalOptions();

    assertNotNull(g);
    assertEquals(c.expectedGroupSize(), g.getGroupSize());
    assertEquals(c.expectedGroupSeparator(), g.getGroupSeparator());

    assertNotNull(f);
    assertEquals(c.expectedFractionalRound(), f.isRoundingEnabled());
    assertEquals(c.expectedFractionalPlaces(), f.getPlaces());
    assertEquals(c.expectedFractionalSeparator(), f.getSeparator());
  }

  static Stream<NumberFormatterCase> groupingAndFractionalCases() {
    return Stream.of(
        new NumberFormatterCase(
            "defaults-only",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: ","
                  fractional:
                    round: true
                    places: 2
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                """,
            true, 3, ",", true, 2, "."),

        new NumberFormatterCase(
            "grouping-override",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: ","
                  fractional:
                    round: false
                    places: 1
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      grouping:
                        group-size: 4
                        separator: " "
                """,
            true, 4, " ", false, 1, "."),

        new NumberFormatterCase(
            "fractional-override",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: ","
                  fractional:
                    round: false
                    places: 1
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      fractional:
                        round: true
                        places: 4
                        separator: "-"
                """,
            true, 3, ",", true, 4, "-"),

        new NumberFormatterCase(
            "full-override",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: ","
                  fractional:
                    round: true
                    places: 2
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      grouping:
                        group-size: 2
                        separator: "_"
                      fractional:
                        round: false
                        places: 8
                        separator: ";"
                """,
            true, 2, "_", false, 8, ";"),

        new NumberFormatterCase(
            "partial-overrides-mix",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                  fractional:
                    round: true
                    places: 2
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      grouping:
                        separator: " "
                """,
            true, 3, " ", true, 2, "."),

        new NumberFormatterCase(
            "override-group-size-only",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: "|"
                  fractional:
                    round: false
                    places: 1
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      grouping:
                        group-size: 5
                """,
            true, 5, "|", false, 1, "."),

        new NumberFormatterCase(
            "no-formatter",
            """
                default-currency: testcur
                currencies:
                  testcur:
                    display-name-singular: Test
                """,
            false, 0, null, false, 0, null),

        new NumberFormatterCase(
            "fractional-override-places-only",
            """
                default-currency: testcur
                default-number-formatter-options:
                  grouping:
                    group-size: 3
                    separator: ","
                  fractional:
                    round: true
                    places: 2
                    separator: "."
                currencies:
                  testcur:
                    display-name-singular: Test
                    number-formatter:
                      fractional:
                        places: 5
                """,
            true, 3, ",", true, 5, "."));
  }

  public record NumberFormatterCase(
      String name,
      String yaml,
      boolean expectFormatterPresent,
      int expectedGroupSize,
      String expectedGroupSeparator,
      boolean expectedFractionalRound,
      int expectedFractionalPlaces,
      String expectedFractionalSeparator) {
  }

  @Test
  void currencyCacheOptions_use_smart_defaults_based_on_default_currency() {
    var config = getStringYamlJConomyConfig(
        """
            default-currency: gold
            currencies:
              gold:
                display-name-singular: Gold
              tokens:
                display-name-singular: Tokens
            """);

    var defaultCurrencyOptions = config.getCurrencyOptions("gold");
    var nonDefaultCurrencyOptions = config.getCurrencyOptions("tokens");

    assertNotNull(defaultCurrencyOptions);
    assertNotNull(nonDefaultCurrencyOptions);

    assertTrue(defaultCurrencyOptions.getCacheOptions().isWarmOnJoinEnabled());
    assertFalse(defaultCurrencyOptions.getCacheOptions().isWarmOnTeleportEnabled());

    assertFalse(nonDefaultCurrencyOptions.getCacheOptions().isWarmOnJoinEnabled());
    assertFalse(nonDefaultCurrencyOptions.getCacheOptions().isWarmOnTeleportEnabled());
  }

  @Test
  void currencyCacheOptions_read_explicit_overrides() {
    var config = getStringYamlJConomyConfig(
        """
            default-currency: gold
            currencies:
              gold:
                display-name-singular: Gold
                cache:
                  warm-on-join: false
                  warm-on-teleport: true
              tokens:
                display-name-singular: Tokens
                cache:
                  warm-on-join: true
                  warm-on-teleport: true
            """);

    var gold = config.getCurrencyOptions("gold");
    var tokens = config.getCurrencyOptions("tokens");

    assertNotNull(gold);
    assertNotNull(tokens);

    assertFalse(gold.getCacheOptions().isWarmOnJoinEnabled());
    assertTrue(gold.getCacheOptions().isWarmOnTeleportEnabled());

    assertTrue(tokens.getCacheOptions().isWarmOnJoinEnabled());
    assertTrue(tokens.getCacheOptions().isWarmOnTeleportEnabled());
  }

  YamlEconomyConfig getStringYamlJConomyConfig(String yaml) {
    var yamlConfigProvider = new StringYamlConfigurationProvider(yaml);
    var jconomyConfig = new DefaultJConomyConfig(() -> yamlConfigProvider.getFileConfiguration());
    var config = new YamlEconomyConfig(jconomyConfig);
    return config;
  }
}