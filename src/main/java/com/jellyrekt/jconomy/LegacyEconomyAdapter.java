package com.jellyrekt.jconomy;

import java.math.BigDecimal;
import java.util.List;

import org.bukkit.OfflinePlayer;

import com.jellyrekt.jconomy.config.JConomyConfig;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class LegacyEconomyAdapter implements Economy {
    private final EconomyImp economy;
    private final JConomyConfig config;

    public LegacyEconomyAdapter(EconomyImp economy, JConomyConfig config) {
        this.economy = economy;
        this.config = config;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        var result = economy.getDefaultCurrency(name);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankBalance'");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankDeposit'");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankHas'");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankWithdraw'");
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }

    @Override
    public String currencyNamePlural() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currencyNamePlural'");
    }

    @Override
    public String currencyNameSingular() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currencyNameSingular'");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteBank'");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }

    @Override
    public String format(double amount) {
        return economy.format(BigDecimal.valueOf(amount));
    }

    @Override
    public int fractionalDigits() {
        return economy.fractionalDigits(null);
    }

    @Override
    public double getBalance(String playerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public double getBalance(String playerName, String world) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }

    @Override
    public List<String> getBanks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBanks'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public boolean has(String playerName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }

    @Override
    public boolean hasAccount(String playerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }

    @Override
    public boolean hasBankSupport() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasBankSupport'");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEnabled'");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }
    
}
