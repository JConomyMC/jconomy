package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.permission.Permission;

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
        var importListHandler = new ImportListCommandHandler(importers);
        var exportListHandler = new ExportListCommandHandler(exporters);

        var base = commandManager.commandBuilder("jconomy");

        commandManager.command(base
                .literal("import")
                .literal("list")
                .permission(Permission.of("jconomy.list.import"))
                .handler(importListHandler::execute));

        commandManager.command(base
                .literal("export")
                .literal("list")
                .permission(Permission.of("jconomy.list.export"))
                .handler(exportListHandler::execute));
    }
}
