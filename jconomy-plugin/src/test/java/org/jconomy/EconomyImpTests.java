package org.jconomy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.accounts.Account;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.config.economy.EconomyConfig;
import org.jconomy.presentation.CurrencyFormatter;

import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;

class EconomyImpTests {

    private PluginContext pluginContext;
    private Logger logger;
    private CurrencyFormatter currencyFormatter;
    private EconomyConfig config;
    private AccountAccess accountAccess;
    private EconomyImp economy;

    @BeforeEach
    void setUp() {
        pluginContext = mock(PluginContext.class);
        logger = Logger.getLogger("test");
        currencyFormatter = mock(CurrencyFormatter.class);
        config = mock(EconomyConfig.class);
        accountAccess = mock(AccountAccess.class);
        economy = new EconomyImp(pluginContext, logger, currencyFormatter, config, accountAccess);
    }

    @Test
    void getBalance_returns_account_balance() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(100));
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        assertEquals(BigDecimal.valueOf(100), economy.getBalance("plugin", id, "world", "gold"));
    }

    @Test
    void getBalance_uses_default_world_when_world_is_null() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "default", "gold", BigDecimal.valueOf(50));
        when(config.getDefaultWorldName()).thenReturn("default");
        when(accountAccess.getByIdAndWorld(id, "default")).thenReturn(Optional.of(account));

        assertEquals(BigDecimal.valueOf(50), economy.getBalance("plugin", id, null, "gold"));
    }

    @Test
    void getBalance_uses_default_currency_when_currency_is_null() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(75));
        when(config.getDefaultCurrency()).thenReturn("gold");
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        assertEquals(BigDecimal.valueOf(75), economy.getBalance("plugin", id, "world", null));
    }

    @Test
    void deposit_increases_balance_and_returns_success() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(100));
        when(config.getDefaultCurrency()).thenReturn("gold");
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        var response = economy.deposit("plugin", id, "world", "gold", BigDecimal.valueOf(50));

        assertEquals(ResponseType.SUCCESS, response.type);
        assertEquals(BigDecimal.valueOf(150), response.amount);
    }

    @Test
    void withdraw_decreases_balance_and_returns_success() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(100));
        when(config.getDefaultCurrency()).thenReturn("gold");
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        var response = economy.withdraw("plugin", id, "world", "gold", BigDecimal.valueOf(30));

        assertEquals(ResponseType.SUCCESS, response.type);
        assertEquals(BigDecimal.valueOf(70), response.amount);
    }

    @Test
    void set_returns_success_response_with_new_balance() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.ZERO);
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        var response = economy.set("plugin", id, "world", "gold", BigDecimal.valueOf(500));

        assertEquals(ResponseType.SUCCESS, response.type);
        assertEquals(BigDecimal.valueOf(500), response.amount);
    }

    @Test
    void set_response_balance_reflects_new_balance_when_currency_is_null() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.ZERO);
        when(config.getDefaultCurrency()).thenReturn("gold");
        when(config.getDefaultWorldName()).thenReturn("world");
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        var response = economy.set("plugin", id, "world", null, BigDecimal.valueOf(250));

        assertEquals(ResponseType.SUCCESS, response.type);
        assertEquals(BigDecimal.valueOf(250), response.balance);
    }

    @Test
    void set_returns_failure_when_account_not_found() {
        var id = UUID.randomUUID();
        when(accountAccess.getByIdAndWorld(eq(id), any())).thenReturn(Optional.empty());

        var response = economy.set("plugin", id, "world", "gold", BigDecimal.valueOf(100));

        assertEquals(ResponseType.FAILURE, response.type);
        assertNull(response.balance);
    }

    @Test
    void has_returns_true_when_balance_exceeds_amount() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(100));
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        assertTrue(economy.has("plugin", id, "world", "gold", BigDecimal.valueOf(50)));
    }

    @Test
    void has_returns_true_when_balance_equals_amount() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(100));
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        assertTrue(economy.has("plugin", id, "world", "gold", BigDecimal.valueOf(100)));
    }

    @Test
    void has_returns_false_when_balance_is_less_than_amount() {
        var id = UUID.randomUUID();
        var account = accountWithBalance(id, "world", "gold", BigDecimal.valueOf(50));
        when(accountAccess.getByIdAndWorld(id, "world")).thenReturn(Optional.of(account));

        assertFalse(economy.has("plugin", id, "world", "gold", BigDecimal.valueOf(100)));
    }

    @Test
    void createAccount_returns_true_on_success() {
        var id = UUID.randomUUID();
        when(accountAccess.createAccount(id, "Alice")).thenReturn(true);

        assertTrue(economy.createAccount(id, "Alice", "world"));
    }

    @Test
    void createAccount_returns_false_when_account_already_exists() {
        var id = UUID.randomUUID();
        when(accountAccess.createAccount(id, "Alice")).thenReturn(false);

        assertFalse(economy.createAccount(id, "Alice", "world"));
    }

    @Test
    void hasAccount_returns_true_when_account_exists() {
        var id = UUID.randomUUID();
        when(accountAccess.getByIdAndWorld(eq(id), any())).thenReturn(Optional.of(new Account(id, "world")));

        assertTrue(economy.hasAccount(id));
    }

    @Test
    void hasAccount_returns_false_when_account_does_not_exist() {
        var id = UUID.randomUUID();
        when(accountAccess.getByIdAndWorld(eq(id), any())).thenReturn(Optional.empty());

        assertFalse(economy.hasAccount(id));
    }

    @Test
    void currencies_delegates_to_config() {
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "silver"));

        assertEquals(Set.of("gold", "silver"), economy.currencies());
    }

    @Test
    void getDefaultCurrency_delegates_to_config() {
        when(config.getDefaultCurrency()).thenReturn("gold");

        assertEquals("gold", economy.getDefaultCurrency("plugin"));
    }

    @Test
    void accountSupportsCurrency_returns_true_for_known_currency() {
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "silver"));

        assertTrue(economy.accountSupportsCurrency("plugin", UUID.randomUUID(), "gold", "world"));
    }

    @Test
    void accountSupportsCurrency_returns_false_for_unknown_currency() {
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "silver"));

        assertFalse(economy.accountSupportsCurrency("plugin", UUID.randomUUID(), "diamonds", "world"));
    }

    @Test
    void renameAccount_renames_and_returns_true_when_account_exists() {
        var id = UUID.randomUUID();
        when(accountAccess.renameAccount(id, "NewName")).thenReturn(true);

        assertTrue(economy.renameAccount(id, "NewName"));
    }

    @Test
    void renameAccount_returns_false_when_account_not_found() {
        var id = UUID.randomUUID();
        when(accountAccess.renameAccount(id, "NewName")).thenReturn(false);

        assertFalse(economy.renameAccount(id, "NewName"));
    }

    private static Account accountWithBalance(UUID id, String world, String currency, BigDecimal amount) {
        var account = new Account(id, world);
        account.setBalance(currency, amount);
        return account;
    }
}
