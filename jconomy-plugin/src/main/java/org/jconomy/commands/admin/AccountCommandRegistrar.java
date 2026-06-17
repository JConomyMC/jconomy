package org.jconomy.commands.admin;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;

import org.jconomy.commands.CommandHandler;

public class AccountCommandRegistrar {

    private final CommandManager<CommandSender> commandManager;
    private final CommandHandler createHandler;
    private final CommandHandler deleteHandler;

    public AccountCommandRegistrar(
            CommandManager<CommandSender> commandManager,
            CommandHandler createHandler,
            CommandHandler deleteHandler) {
        this.commandManager = commandManager;
        this.createHandler = createHandler;
        this.deleteHandler = deleteHandler;
    }

    public void register() {
        var base = commandManager.commandBuilder("jconomy").literal("account");

        commandManager.command(base
                .literal("create")
                .required("player", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.account.create"))
                .handler(createHandler::execute));

        commandManager.command(base
                .literal("delete")
                .required("player", StringParser.stringParser())
                .permission(Permission.of("jconomy.admin.account.delete"))
                .handler(deleteHandler::execute));
    }
}
