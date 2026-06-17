package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.Balance;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

public class BalanceSetCommandHandler {

    private final BalanceAccess balanceAccess;
    private final EconomyConfig economyConfig;
    private final PlayerResolver playerResolver;

    public BalanceSetCommandHandler(
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
        String amountStr = context.get("amount");

        var player = AdminCommandUtil.resolvePlayer(sender, playerName, playerResolver);
        if (player == null) return;

        if (!AdminCommandUtil.validateCurrency(sender, currency, economyConfig)) return;

        var amount = AdminCommandUtil.parseAmount(sender, amountStr);
        if (amount == null) return;

        var world = economyConfig.getDefaultWorldName();
        var balance = balanceAccess.get(player.getUniqueId(), world, currency)
                .orElse(new Balance(player.getUniqueId(), world, currency));
        balance.setAmount(amount);
        balanceAccess.save(balance);

        sender.sendMessage("Set " + playerName + "'s " + currency + " balance to " + amount.toPlainString());
    }
}
