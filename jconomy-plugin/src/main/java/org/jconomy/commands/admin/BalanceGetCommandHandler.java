package org.jconomy.commands.admin;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

public class BalanceGetCommandHandler {

    private final AccountAccess accountAccess;
    private final EconomyConfig economyConfig;
    private final PlayerResolver playerResolver;

    public BalanceGetCommandHandler(
            AccountAccess accountAccess,
            EconomyConfig economyConfig,
            PlayerResolver playerResolver) {
        this.accountAccess = accountAccess;
        this.economyConfig = economyConfig;
        this.playerResolver = playerResolver;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        String playerName = context.get("player");
        String currency = context.get("currency");

        var player = AdminCommandUtil.resolvePlayer(sender, playerName, playerResolver);
        if (player == null) return;

        if (!AdminCommandUtil.validateCurrency(sender, currency, economyConfig)) return;

        var world = economyConfig.getDefaultWorldName();
        var balance = accountAccess.getByIdAndWorld(player.getUniqueId(), world)
                .map(a -> a.getBalance(currency))
                .orElse(BigDecimal.ZERO);

        sender.sendMessage(playerName + "'s " + currency + " balance: " + balance.toPlainString());
    }
}
