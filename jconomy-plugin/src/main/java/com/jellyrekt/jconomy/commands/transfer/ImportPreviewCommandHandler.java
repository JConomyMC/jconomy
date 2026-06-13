package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.TransferImporter;
import com.jellyrekt.jconomy.transfer.TransferPreview;

public class ImportPreviewCommandHandler {

    public void execute(CommandContext<CommandSender> context) {
        var sender = context.sender();
        TransferImporter importer = context.get("provider");
        TransferPreview preview = importer.preview();
        sendSummary(sender, preview);
    }

    private void sendSummary(CommandSender sender, TransferPreview preview) {
        sender.sendMessage("=== Import Preview ===");
        sender.sendMessage("Total accounts: " + preview.totalAccounts());
        sender.sendMessage("New accounts: " + preview.newAccounts());
        sender.sendMessage("Existing accounts: " + preview.existingAccounts());
        sender.sendMessage("Conflicts: " + preview.conflicts());
        sender.sendMessage("Currencies affected: " + String.join(", ", preview.currenciesAffected()));
    }
}
