package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;

public class AccountCreateCommandHandler {

    private final AccountAccess accountAccess;
    private final PlayerResolver playerResolver;

    public AccountCreateCommandHandler(AccountAccess accountAccess, PlayerResolver playerResolver) {
        this.accountAccess = accountAccess;
        this.playerResolver = playerResolver;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        String playerName = context.get("player");

        var player = AdminCommandUtil.resolvePlayer(sender, playerName, playerResolver);
        if (player == null) return;

        var created = accountAccess.createAccount(player.getUniqueId(), player.getName());
        if (created) {
            sender.sendMessage("Created account for " + playerName);
        } else {
            sender.sendMessage("Account already exists for " + playerName);
        }
    }
}
