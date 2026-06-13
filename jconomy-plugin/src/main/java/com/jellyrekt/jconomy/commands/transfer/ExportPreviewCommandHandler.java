package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.TransferExporter;
import com.jellyrekt.jconomy.transfer.TransferPreview;

public class ExportPreviewCommandHandler {

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        TransferExporter exporter = context.get("provider");
        TransferPreview preview = exporter.preview();
        sendSummary(sender, preview);
    }

    private void sendSummary(CommandSender sender, TransferPreview preview) {
        sender.sendMessage("=== Export Preview ===");
        sender.sendMessage("Total accounts: " + preview.totalAccounts());
        sender.sendMessage("New accounts: " + preview.newAccounts());
        sender.sendMessage("Existing accounts: " + preview.existingAccounts());
        sender.sendMessage("Conflicts: " + preview.conflicts());
        sender.sendMessage("Currencies affected: " + String.join(", ", preview.currenciesAffected()));
    }
}
