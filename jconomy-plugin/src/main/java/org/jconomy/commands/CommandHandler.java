package org.jconomy.commands;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

public interface CommandHandler {
    void execute(CommandContext<CommandSender> context);
}
