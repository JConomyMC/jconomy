package org.jconomy.commands.transfer;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.permission.Permission;

import org.jconomy.transfer.ConflictPolicy;
import org.jconomy.transfer.TransferExporter;
import org.jconomy.transfer.TransferImporter;

public class TransferCommandRegistrar {

    private final CommandManager<CommandSender> commandManager;
    private final List<TransferImporter> importers;
    private final List<TransferExporter> exporters;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;
    private final TransferPlanStore planStore;

    public TransferCommandRegistrar(
            CommandManager<CommandSender> commandManager,
            List<TransferImporter> importers,
            List<TransferExporter> exporters,
            BukkitScheduler scheduler,
            JavaPlugin plugin,
            TransferPlanStore planStore) {
        this.commandManager = commandManager;
        this.importers = importers;
        this.exporters = exporters;
        this.scheduler = scheduler;
        this.plugin = plugin;
        this.planStore = planStore;
    }

    public void register() {
        var importListHandler = new ImportListCommandHandler(importers);
        var exportListHandler = new ExportListCommandHandler(exporters);
        var importPreviewHandler = new ImportPreviewCommandHandler(planStore, scheduler, plugin);
        var exportPreviewHandler = new ExportPreviewCommandHandler(planStore, scheduler, plugin);
        var importExecuteHandler = new ImportExecuteCommandHandler(planStore, scheduler, plugin);
        var exportExecuteHandler = new ExportExecuteCommandHandler(planStore, scheduler, plugin);

        ParserDescriptor<CommandSender, TransferImporter> importerParser =
                ParserDescriptor.of(new TransferImporterParser<>(importers), TransferImporter.class);
        ParserDescriptor<CommandSender, TransferExporter> exporterParser =
                ParserDescriptor.of(new TransferExporterParser<>(exporters), TransferExporter.class);
        ParserDescriptor<CommandSender, ConflictPolicy> policyParser =
                EnumParser.enumParser(ConflictPolicy.class);

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
                .optional("policy", policyParser, DefaultValue.constant(ConflictPolicy.SKIP))
                .permission(Permission.of("jconomy.preview.import"))
                .handler(importPreviewHandler::execute));

        commandManager.command(exportBase
                .required("provider", exporterParser)
                .literal("preview")
                .optional("policy", policyParser, DefaultValue.constant(ConflictPolicy.SKIP))
                .permission(Permission.of("jconomy.preview.export"))
                .handler(exportPreviewHandler::execute));

        commandManager.command(importBase
                .required("provider", importerParser)
                .literal("execute")
                .permission(Permission.of("jconomy.execute.import"))
                .handler(importExecuteHandler::execute));

        commandManager.command(exportBase
                .required("provider", exporterParser)
                .literal("execute")
                .permission(Permission.of("jconomy.execute.export"))
                .handler(exportExecuteHandler::execute));
    }
}
