package com.jellyrekt.jconomy;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.accounts.Account;
import com.jellyrekt.jconomy.accounts.AccountName;
import com.jellyrekt.jconomy.accounts.AccountNameRepository;
import com.jellyrekt.jconomy.accounts.AccountAccess;
import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.presentation.CurrencyFormatter;

import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;

public class EconomyImp implements Economy {
    private final JavaPlugin plugin;
    private final CurrencyFormatter currencyFormatter;
    private final JConomyConfig config;
    private final AccountAccess accountRepository;
    private final AccountNameRepository accountNameRepository;

    public EconomyImp(JavaPlugin plugin, CurrencyFormatter currencyFormatter, JConomyConfig config,
            AccountAccess accountRepository, AccountNameRepository accountNameRepository) {
        this.plugin = plugin;
        this.currencyFormatter = currencyFormatter;
        this.config = config;
        this.accountRepository = accountRepository;
        this.accountNameRepository = accountNameRepository;
    }
    
    private Account getAccountOrThrow(UUID accountId, String world) {
        return accountRepository.getByIdAndWorld(accountId, worldNameOrDefault(world))
            .orElseThrow(() -> {
                var message = String.format("Account (accountId='%s',world='%s') not found", accountId, world);
                throw new NoSuchElementException(message);
            });
    }
    
    private String currencyOrDefault(String currency) {
        if (currency == null) {
            return config.getDefaultCurrency();
        }
        return currency;
    }

    private String worldNameOrDefault(String worldName) {
        if (worldName == null) {
            return config.getDefaultWorldName();
        }
        return worldName;
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return plugin.getName();
    }

    @Override
    public boolean hasSharedAccountSupport() {
        return false;
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        return true;
    }

    @Override
    public int fractionalDigits(String pluginName) {
        return -1;
    }

    @Override
    public String format(BigDecimal amount) {
        return format(null, amount, getDefaultCurrency(null));
    }

    @Override
    public String format(String pluginName, BigDecimal amount) {
        return format(pluginName, amount, getDefaultCurrency(pluginName));
    }

    @Override
    public String format(BigDecimal amount, String currency) {
        return format(null, amount, currency);
    }

    @Override
    public String format(String pluginName, BigDecimal amount, String currency) {
        return currencyFormatter.format(amount, currency);
    }

    @Override
    public boolean hasCurrency(String currency) {
        return currencies().stream().anyMatch(c -> c == currency);
    }

    @Override
    public String getDefaultCurrency(String pluginName) {
        return config.getDefaultCurrency();
    }

    @Override
    public String defaultCurrencyNamePlural(String pluginName) {
        var defaultCurrency = getDefaultCurrency(pluginName);
        return config.getCurrencyOptions(defaultCurrency).getDisplayNamePlural();
    }

    @Override
    public String defaultCurrencyNameSingular(String pluginName) {
        var defaultCurrency = getDefaultCurrency(pluginName);
        return config.getCurrencyOptions(defaultCurrency).getDisplayNameSingular();
    }

    @Override
    public Collection<String> currencies() {
        return config.getAllCurrencyNames();
    }

    @Override
    public boolean createAccount(UUID accountId, String name) {
        return createAccount(accountId, name, false);
    }

    @Override
    public boolean createAccount(UUID accountId, String name, boolean isPlayerAccount) {
        return createAccount(accountId, name, null, false);
    }

    @Override
    public boolean createAccount(UUID accountId, String name, String worldName) {
        return createAccount(accountId, name, worldName, false);
    }

    @Override
    public boolean createAccount(UUID accountId, String name, String worldName, boolean isPlayerAccount) {
        try {
            var account = new Account(accountId, name, worldNameOrDefault(worldName));
            accountRepository.save(account);
            return true;
        } catch (Exception ex) {
            var message = String.format("Unable to create account(accountId='%s',name='%s',world='%s'): %s",
                    accountId, name, worldName, ex.getMessage());
            plugin.getLogger().warning(message);
            return false;
        }
    }

    @Override
    public Map<UUID, String> getUUIDNameMap() {
        return accountNameRepository.getAll().stream()
                .collect(Collectors.toMap(AccountName::getAccountId, AccountName::getName));
    }

    @Override
    public Optional<String> getAccountName(UUID accountId) {
        return accountNameRepository.getByAccountId(accountId).map(AccountName::getName);
    }

    @Override
    public boolean accountSupportsCurrency(String plugin, UUID accountId, String currency) {
        return accountSupportsCurrency(plugin, accountId, currency, null);
    }

    @Override
    public boolean accountSupportsCurrency(String plugin, UUID accountId, String currency,
            String worldName) {
        return config.getAllCurrencyNames().contains(currency);
    }

    @Override
    public BigDecimal getBalance(String pluginName, UUID accountId) {
        return getBalance(pluginName, accountId, null);
    }

    @Override
    public BigDecimal getBalance(String pluginName, UUID accountId, String world) {
        return getBalance(pluginName, accountId, world, null);
    }

    @Override
    public BigDecimal getBalance(String pluginName, UUID accountId, String world, String currency) {
        return getAccountOrThrow(accountId, world).getBalance(
                currency != null ? currency : config.getDefaultCurrency());
    }

    @Override
    public boolean has(String pluginName, UUID accountId, BigDecimal amount) {
        return has(pluginName, accountId, null, amount);
    }

    @Override
    public boolean has(String pluginName, UUID accountId, String world, BigDecimal amount) {
        return has(pluginName, accountId, world, config.getDefaultCurrency(), amount);
    }

    @Override
    public boolean has(String pluginName, UUID accountId, String world, String currency,
            BigDecimal amount) {
        return getBalance(pluginName, accountId, world, currency).compareTo(amount) >= 0;
    }

    @Override
    public EconomyResponse set(String pluginName, UUID accountID, BigDecimal amount) {
        return set(pluginName, accountID, null, amount);
    }

    @Override
    public EconomyResponse set(String pluginName, UUID accountID, String worldName, BigDecimal amount) {
        return set(pluginName, accountID, worldName, null, amount);
    }
    
    @Override
    public EconomyResponse set(String pluginName, UUID accountID, String worldName, String currency, BigDecimal amount) {
        try {
            var account = getAccountOrThrow(accountID, worldName);
            account.setBalance(currencyOrDefault(currency), amount);
            accountRepository.save(account);

            return new EconomyResponse(amount, account.getBalance(currency), ResponseType.SUCCESS, "");
        } catch (Exception ex) {
            return new EconomyResponse(BigDecimal.ZERO, null, ResponseType.FAILURE, ex.getMessage());
        }
    }

    @Override
    public EconomyResponse withdraw(String pluginName, UUID accountId, BigDecimal amount) {
        return withdraw(pluginName, accountId, null, amount);
    }

    @Override
    public EconomyResponse withdraw(String pluginName, UUID accountId, String worldName,
            BigDecimal amount) {
        return withdraw(pluginName, accountId, worldName, null, amount);
    }

    @Override
    public EconomyResponse withdraw(String pluginName, UUID accountId, String worldName,
            String currency, BigDecimal amount) {
        currency = currencyOrDefault(currency);

        var balance = getBalance(pluginName, accountId, worldName, currency);
        var response = set(pluginName, accountId, worldName, currency, balance.subtract(amount));

        var newBalance = response.transactionSuccess() ? response.balance : balance;

        return new EconomyResponse(response.amount, newBalance, response.type, response.errorMessage);
    }

    @Override
    public EconomyResponse deposit(String pluginName, UUID accountId, BigDecimal amount) {
        return deposit(pluginName, accountId, null, amount);
    }

    @Override
    public EconomyResponse deposit(String pluginName, UUID accountId, String worldName,
            BigDecimal amount) {
        return deposit(pluginName, accountId, worldName, null, amount);
    }

    @Override
    public EconomyResponse deposit(String pluginName, UUID accountId, String worldName,
            String currency, BigDecimal amount) {
        currency = currencyOrDefault(currency);

        var balance = getBalance(pluginName, accountId, worldName, currency);
        var response = set(pluginName, accountId, worldName, currency, balance.add(amount));

        var newBalance = response.transactionSuccess() ? response.balance : balance;

        return new EconomyResponse(response.amount, newBalance, response.type, response.errorMessage);
    }

    private boolean accountsNotSupported(String pluginName, String calledMethod) {
        plugin.getLogger().warning(String.format("%s called %s, but shared accounts are not supported.", pluginName, calledMethod));
        return false;
    }

    @Override
    public boolean createSharedAccount(String arg0, UUID arg1, String arg2,
            UUID arg3) {
        return accountsNotSupported(arg0, "createSharedAccount");
    }

    @Override
    public boolean deleteAccount(String arg0, UUID arg1) {
        return accountsNotSupported(arg0, "deleteAccount");
    }

    @Override
    public boolean isAccountOwner(String arg0, UUID arg1, UUID arg2) {
        return accountsNotSupported(arg0, "isAccountOwner");
    }

    @Override
    public boolean setOwner(String arg0, UUID arg1, UUID arg2) {
        return accountsNotSupported(arg0, "setOwner");
    }

    @Override
    public boolean isAccountMember(String arg0, UUID arg1, UUID arg2) {
        return accountsNotSupported(arg0, "isAccountMember");
    }

    @Override
    public boolean addAccountMember(String plugin, UUID accountId, UUID arg2) {
        return addAccountMember(plugin, accountId, arg2, new AccountPermission[0]);
    }

    @Override
    public boolean addAccountMember(String arg0, UUID arg1, UUID arg2,
            AccountPermission... arg3) {
        return accountsNotSupported(arg0, "addAccountMember");
    }

    @Override
    public boolean removeAccountMember(String arg0, UUID arg1, UUID arg2) {
        return accountsNotSupported(arg0, "removeAccountMember");
    }

    @Override
    public boolean hasAccountPermission(String arg0, UUID arg1, UUID arg2,
            AccountPermission arg3) {
        return accountsNotSupported(arg0, "hasAccountPermission");
    }

    @Override
    public boolean hasAccount(UUID accountId) {
        return accountsNotSupported("A plugin", "hasAccount");
    }

    @Override
    public boolean hasAccount(UUID accountId, String worldName) {
        return accountsNotSupported("A plugin", "hasAccount");
    }

    @Override
    public boolean renameAccount(UUID accountId, String name) {
        return renameAccount(null, accountId, name);
    }

    @Override
    public boolean renameAccount(String pluginName, UUID accountId, String name) {
        var result = accountNameRepository.getByAccountId(accountId);
        if (result.isPresent()) {
            var accountName = result.get();
            accountName.setName(name);
            accountNameRepository.save(accountName);
            return true;
        }

        plugin.getLogger().warning("Account(accountId='%s') does not exist");
        return false;
    }

    @Override
    public boolean updateAccountPermission(String pluginName, UUID accountId, UUID name,
            AccountPermission arg3, boolean arg4) {
        return accountsNotSupported(pluginName, "updateAccountPermission");
    }
    
}
