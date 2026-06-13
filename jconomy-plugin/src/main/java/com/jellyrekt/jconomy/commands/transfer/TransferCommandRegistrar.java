package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;

import com.jellyrekt.jconomy.transfer.TransferExporter;
import com.jellyrekt.jconomy.transfer.TransferImporter;

public class TransferCommandRegistrar {

    private final CommandManager<CommandSender> commandManager;
    private final List<TransferImporter> importers;
    private final List<TransferExporter> exporters;

    public TransferCommandRegistrar(
            CommandManager<CommandSender> commandManager,
            List<TransferImporter> importers,
            List<TransferExporter> exporters) {
        this.commandManager = commandManager;
        this.importers = importers;
        this.exporters = exporters;
    }

    public void register() {
    }
}
