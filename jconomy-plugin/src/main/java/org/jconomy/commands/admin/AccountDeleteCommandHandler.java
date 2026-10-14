package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.AccountDeleter;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.CommandHandler;

public class AccountDeleteCommandHandler implements CommandHandler {

    private final AccountDeleter accountDeleter;
    private final PlayerResolver playerResolver;

    public AccountDeleteCommandHandler(AccountDeleter accountDeleter, PlayerResolver playerResolver) {
        this.accountDeleter = accountDeleter;
        this.playerResolver = playerResolver;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        String playerName = context.get("player");

        var player = AdminCommandUtil.resolvePlayer(sender, playerName, playerResolver);
        if (player == null) return;

        accountDeleter.delete(player.getUniqueId());
        sender.sendMessage("Deleted account for " + playerName);
    }
}
