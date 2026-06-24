package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.accounts.Account;
import org.jconomy.accounts.AccountAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.commands.CommandHandler;

public class AccountCreateCommandHandler implements CommandHandler {

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

        var id = player.getUniqueId();
        var created = accountAccess.getAccount(id).isEmpty();
        if (created) accountAccess.save(new Account(id, player.getName()));
        if (created) {
            sender.sendMessage("Created account for " + playerName);
        } else {
            sender.sendMessage("Account already exists for " + playerName);
        }
    }
}
