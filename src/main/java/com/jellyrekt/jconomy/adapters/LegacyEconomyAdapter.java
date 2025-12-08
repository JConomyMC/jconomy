package com.jellyrekt.jconomy.adapters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.jellyrekt.jconomy.EconomyImp;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class LegacyEconomyAdapter implements Economy {
    private static final EconomyResponse ResponseBanksNotSupported = new EconomyResponse(0, 0,
            ResponseType.NOT_IMPLEMENTED, "JConomy does not support banking for Vault legacy");

    private final EconomyImp economy;
    private final EconomyResponseMapper responseMapper;

    public LegacyEconomyAdapter(EconomyImp economy, EconomyResponseMapper responseMapper) {
        this.economy = economy;
        this.responseMapper = responseMapper;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return ResponseBanksNotSupported;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return economy.createAccount(player.getUniqueId(), player.getName(), true);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(getOfflinePlayer(playerName), worldName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return economy.createAccount(player.getUniqueId(), player.getName(), worldName, true);
    }

    @Override
    public String currencyNamePlural() {
        return economy.defaultCurrencyNamePlural(null);
    }

    @Override
    public String currencyNameSingular() {
        return economy.defaultCurrencyNameSingular(null);
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(getOfflinePlayer(playerName), amount);
    }

    private OfflinePlayer getOfflinePlayer(String playerName) {
        var player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return player;
        }
        // I am assuming this is faster than a scheduled API call,
        // and ok with not supporting players who have never joined.
        Optional<OfflinePlayer> offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
            .filter(p -> p.getName().equals(playerName))
            .findFirst();
        return offlinePlayer.get();
    }

    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        var response = economy.deposit(null, player.getUniqueId(), new BigDecimal(amount));
        return responseMapper.getLegacyResponse(response);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        var player = getOfflinePlayer(playerName);
        return depositPlayer(player, worldName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        var response = economy.deposit(null, player.getUniqueId(), worldName, new BigDecimal(amount));
        return responseMapper.getLegacyResponse(response);
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
        return getBalance(getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(null, player.getUniqueId()).doubleValue();
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(getOfflinePlayer(playerName), world);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return economy.getBalance(null, player.getUniqueId(), world).doubleValue();
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public String getName() {
        return economy.getName();
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return economy.has(null, player.getUniqueId(), new BigDecimal(amount));
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(getOfflinePlayer(playerName), worldName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return economy.has(null, player.getUniqueId(), worldName, new BigDecimal(amount));
    }

    @Override
    public boolean hasAccount(String playerName) {
        return hasAccount(getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return economy.hasAccount(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(getOfflinePlayer(playerName), worldName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return economy.hasAccount(player.getUniqueId(), worldName);
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return ResponseBanksNotSupported;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return ResponseBanksNotSupported;
    }

    @Override
    public boolean isEnabled() {
        return economy.isEnabled();
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        var response = economy.withdraw(null, player.getUniqueId(), new BigDecimal(amount));
        return responseMapper.getLegacyResponse(response);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(getOfflinePlayer(playerName), worldName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        var response = economy.withdraw(null, player.getUniqueId(), worldName, new BigDecimal(amount));
        return responseMapper.getLegacyResponse(response);
    }
    
}
