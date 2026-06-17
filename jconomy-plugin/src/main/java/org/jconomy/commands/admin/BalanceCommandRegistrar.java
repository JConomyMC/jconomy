package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;

import org.jconomy.commands.CommandHandler;

public class BalanceCommandRegistrar {

    private final CommandManager<CommandSender> commandManager;
    private final CommandHandler getHandler;
    private final CommandHandler setHandler;
    private final CommandHandler addHandler;
    private final CommandHandler removeHandler;

    public BalanceCommandRegistrar(
            CommandManager<CommandSender> commandManager,
            CommandHandler getHandler,
            CommandHandler setHandler,
            CommandHandler addHandler,
            CommandHandler removeHandler) {
        this.commandManager = commandManager;
        this.getHandler = getHandler;
        this.setHandler = setHandler;
        this.addHandler = addHandler;
        this.removeHandler = removeHandler;
    }

    public void register() {
        var base = commandManager.commandBuilder("jconomy").literal("balance");

        commandManager.command(base
                .literal("get")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.get"))
                .handler(getHandler::execute));

        commandManager.command(base
                .literal("set")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.set"))
                .handler(setHandler::execute));

        commandManager.command(base
                .literal("add")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.add"))
                .handler(addHandler::execute));

        commandManager.command(base
                .literal("remove")
                .required("player", StringParser.stringParser())
                .required("currency", StringParser.stringParser())
                .required("amount", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.balance.remove"))
                .handler(removeHandler::execute));
    }
}
