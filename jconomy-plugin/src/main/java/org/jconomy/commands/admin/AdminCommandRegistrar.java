package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.adapters.PlayerResolver;
import org.jconomy.config.economy.EconomyConfig;

public class AdminCommandRegistrar {

    private final CommandManager<CommandSender> commandManager;
    private final BalanceAccess balanceAccess;
    private final AccountAccess accountAccess;
    private final EconomyConfig economyConfig;
    private final PlayerResolver playerResolver;

    public AdminCommandRegistrar(
            CommandManager<CommandSender> commandManager,
            BalanceAccess balanceAccess,
            AccountAccess accountAccess,
            EconomyConfig economyConfig,
            PlayerResolver playerResolver) {
        this.commandManager = commandManager;
        this.balanceAccess = balanceAccess;
        this.accountAccess = accountAccess;
        this.economyConfig = economyConfig;
        this.playerResolver = playerResolver;
    }

    public void register() {
        var getHandler = new BalanceGetCommandHandler(balanceAccess, economyConfig, playerResolver);
        var setHandler = new BalanceSetCommandHandler(balanceAccess, economyConfig, playerResolver);
        var addHandler = new BalanceAddCommandHandler(balanceAccess, economyConfig, playerResolver);
        var removeHandler = new BalanceRemoveCommandHandler(balanceAccess, economyConfig, playerResolver);
        var createHandler = new AccountCreateCommandHandler(accountAccess, playerResolver);
        var deleteHandler = new AccountDeleteCommandHandler(accountAccess, balanceAccess, playerResolver);

        var base = commandManager.commandBuilder("jconomy");
        var balanceBase = base.literal("balance");
        var accountBase = base.literal("account");

        commandManager.command(balanceBase
                .literal("get")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.get"))
                .handler(getHandler::execute));

        commandManager.command(balanceBase
                .literal("set")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.set"))
                .handler(setHandler::execute));

        commandManager.command(balanceBase
                .literal("add")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.add"))
                .handler(addHandler::execute));

        commandManager.command(balanceBase
                .literal("remove")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.remove"))
                .handler(removeHandler::execute));

        commandManager.command(accountBase
                .literal("create")
                .required("player", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.account.create"))
                .handler(createHandler::execute));

        commandManager.command(accountBase
                .literal("delete")
                .required("player", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.account.delete"))
                .handler(deleteHandler::execute));
    }
}
