package com.jellyrekt.jconomy;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.config.JConomyConfig;
import com.jellyrekt.jconomy.presentation.CurrencyFormatter;

import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;

public class EconomyImp implements Economy {

    private final JavaPlugin plugin;
    private final CurrencyFormatter currencyFormatter;
    private final JConomyConfig config;

    public EconomyImp(JavaPlugin plugin, CurrencyFormatter currencyFormatter, JConomyConfig config) {
        this.plugin = plugin;
        this.currencyFormatter = currencyFormatter;
        this.config = config;
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
        // TODO Auto-generated method stub
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
    public String getDefaultCurrency(String pluginName) {
        return config.getDefaultCurrency();
    }

    @Override
    public boolean accountSupportsCurrency(String arg0, UUID arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountSupportsCurrency'");
    }

    @Override
    public boolean accountSupportsCurrency(String arg0, UUID arg1, String arg2,
            String arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'accountSupportsCurrency'");
    }

    @Override
    public boolean addAccountMember(String arg0, UUID arg1, UUID arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAccountMember'");
    }

    @Override
    public boolean addAccountMember(String arg0, UUID arg1, UUID arg2,
            AccountPermission... arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAccountMember'");
    }

    @Override
    public boolean createAccount(UUID arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID arg0, String arg1, boolean arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createAccount(UUID arg0, String arg1, String arg2, boolean arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    @Override
    public boolean createSharedAccount(String arg0, UUID arg1, String arg2,
            UUID arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createSharedAccount'");
    }

    @Override
    public Collection<String> currencies() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currencies'");
    }

    @Override
    public String defaultCurrencyNamePlural(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'defaultCurrencyNamePlural'");
    }

    @Override
    public String defaultCurrencyNameSingular(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'defaultCurrencyNameSingular'");
    }

    @Override
    public boolean deleteAccount(String arg0, UUID arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAccount'");
    }

    @Override
    public EconomyResponse deposit(String arg0, UUID arg1, BigDecimal arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }

    @Override
    public EconomyResponse deposit(String arg0, UUID arg1, String arg2,
            BigDecimal arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }

    @Override
    public EconomyResponse deposit(String arg0, UUID arg1, String arg2,
            String arg3, BigDecimal arg4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deposit'");
    }

    @Override
    public Optional<String> getAccountName(UUID arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAccountName'");
    }

    @Override
    public BigDecimal getBalance(String arg0, UUID arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public BigDecimal getBalance(String arg0, UUID arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public BigDecimal getBalance(String arg0, UUID arg1, String arg2,
            String arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public Map<UUID, String> getUUIDNameMap() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUUIDNameMap'");
    }

    @Override
    public boolean has(String arg0, UUID arg1, BigDecimal arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(String arg0, UUID arg1, String arg2, BigDecimal arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(String arg0, UUID arg1, String arg2, String arg3,
            BigDecimal arg4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean hasAccount(UUID arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(UUID arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccountPermission(String arg0, UUID arg1, UUID arg2,
            AccountPermission arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccountPermission'");
    }

    @Override
    public boolean hasCurrency(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasCurrency'");
    }

    @Override
    public boolean isAccountMember(String arg0, UUID arg1, UUID arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountMember'");
    }

    @Override
    public boolean isAccountOwner(String arg0, UUID arg1, UUID arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAccountOwner'");
    }

    @Override
    public boolean removeAccountMember(String arg0, UUID arg1, UUID arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeAccountMember'");
    }

    @Override
    public boolean renameAccount(UUID arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameAccount'");
    }

    @Override
    public boolean renameAccount(String arg0, UUID arg1, String arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'renameAccount'");
    }

    @Override
    public boolean setOwner(String arg0, UUID arg1, UUID arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setOwner'");
    }

    @Override
    public boolean updateAccountPermission(String arg0, UUID arg1, UUID arg2,
            AccountPermission arg3, boolean arg4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateAccountPermission'");
    }

    @Override
    public EconomyResponse withdraw(String arg0, UUID arg1, BigDecimal arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdraw'");
    }

    @Override
    public EconomyResponse withdraw(String arg0, UUID arg1, String arg2,
            BigDecimal arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdraw'");
    }

    @Override
    public EconomyResponse withdraw(String arg0, UUID arg1, String arg2,
            String arg3, BigDecimal arg4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdraw'");
    }
    
}
