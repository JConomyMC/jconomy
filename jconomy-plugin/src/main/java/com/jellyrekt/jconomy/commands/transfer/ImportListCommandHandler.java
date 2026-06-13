package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.TransferImporter;

public class ImportListCommandHandler {

    private final List<TransferImporter> importers;

    public ImportListCommandHandler(List<TransferImporter> importers) {
        this.importers = importers;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        if (importers.isEmpty()) {
            sender.sendMessage("No import providers are registered.");
            return;
        }
        importers.forEach(i -> sender.sendMessage(i.getName()));
    }
}
