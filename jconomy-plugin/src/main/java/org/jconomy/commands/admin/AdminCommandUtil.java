package org.jconomy.commands.admin;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

class AdminCommandUtil {

    private AdminCommandUtil() {}

    static OfflinePlayer resolvePlayer(CommandSender sender, String playerName, PlayerResolver playerResolver) {
        try {
            return playerResolver.resolve(playerName);
        } catch (NoSuchElementException e) {
            sender.sendMessage("Unknown player: " + playerName);
            return null;
        }
    }

    static boolean validateCurrency(CommandSender sender, String currency, EconomyConfig economyConfig) {
        if (!economyConfig.getAllCurrencyNames().contains(currency)) {
            sender.sendMessage("Unknown currency: " + currency);
            return false;
        }
        return true;
    }

    static BigDecimal parseAmount(CommandSender sender, String amountStr) {
        try {
            return new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount: " + amountStr);
            return null;
        }
    }
}
