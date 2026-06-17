package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;

public class AccountDeleteCommandHandler {

    private final AccountAccess accountAccess;
    private final BalanceAccess balanceAccess;
    private final PlayerResolver playerResolver;

    public AccountDeleteCommandHandler(AccountAccess accountAccess, BalanceAccess balanceAccess, PlayerResolver playerResolver) {
        this.accountAccess = accountAccess;
        this.balanceAccess = balanceAccess;
        this.playerResolver = playerResolver;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        String playerName = context.get("player");

        var player = AdminCommandUtil.resolvePlayer(sender, playerName, playerResolver);
        if (player == null) return;

        var playerId = player.getUniqueId();
        balanceAccess.deleteByAccount(playerId);
        accountAccess.deleteAccount(playerId);
        sender.sendMessage("Deleted account for " + playerName);
    }
}
