package com.jellyrekt.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.ParserDescriptor;
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
        var importPreviewHandler = new ImportPreviewCommandHandler();
        var exportPreviewHandler = new ExportPreviewCommandHandler();

        ParserDescriptor<CommandSender, TransferImporter> importerParser =
                ParserDescriptor.of(new TransferImporterParser<>(importers), TransferImporter.class);
        ParserDescriptor<CommandSender, TransferExporter> exporterParser =
                ParserDescriptor.of(new TransferExporterParser<>(exporters), TransferExporter.class);

        var base = commandManager.commandBuilder("jconomy");
        var importBase = base.literal("import");
        var exportBase = base.literal("export");

        commandManager.command(importBase
                .literal("list")
                .permission(Permission.of("jconomy.list.import"))
                .handler(importListHandler::execute));

        commandManager.command(exportBase
                .literal("list")
                .permission(Permission.of("jconomy.list.export"))
                .handler(exportListHandler::execute));

        commandManager.command(importBase
                .required("provider", importerParser)
                .literal("preview")
                .permission(Permission.of("jconomy.preview.import"))
                .handler(importPreviewHandler::execute));

        commandManager.command(exportBase
                .required("provider", exporterParser)
                .literal("preview")
                .permission(Permission.of("jconomy.preview.export"))
                .handler(exportPreviewHandler::execute));
    }
}
