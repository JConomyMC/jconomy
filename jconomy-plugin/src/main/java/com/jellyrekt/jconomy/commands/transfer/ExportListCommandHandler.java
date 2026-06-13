package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.TransferExporter;

public class ExportListCommandHandler {

    private final List<TransferExporter> exporters;

    public ExportListCommandHandler(List<TransferExporter> exporters) {
        this.exporters = exporters;
    }

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        if (exporters.isEmpty()) {
            sender.sendMessage("No export providers are registered.");
            return;
        }
        exporters.forEach(e -> sender.sendMessage(e.getName()));
    }
}
