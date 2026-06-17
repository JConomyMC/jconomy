package org.jconomy.commands.admin;

import java.math.BigDecimal;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.CommandHandler;
import org.jconomy.config.economy.EconomyConfig;

public class BalanceGetCommandHandler implements CommandHandler {

    private final BalanceAccess balanceAccess;
    private final EconomyConfig economyConfig;
    private final PlayerResolver playerResolver;

    public BalanceGetCommandHandler(
            BalanceAccess balanceAccess,
            EconomyConfig economyConfig,
            PlayerResolver playerResolver) {
        this.balanceAccess = balanceAccess;
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
        var balance = balanceAccess.get(player.getUniqueId(), world, currency)
                .map(b -> b.getAmount())
                .orElse(BigDecimal.ZERO);

        sender.sendMessage(playerName + "'s " + currency + " balance: " + balance.toPlainString());
    }
}
