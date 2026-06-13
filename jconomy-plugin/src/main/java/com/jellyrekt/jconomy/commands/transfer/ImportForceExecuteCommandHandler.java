package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferImporter;

public class ImportForceExecuteCommandHandler {

    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    public ImportForceExecuteCommandHandler(BukkitScheduler scheduler, JavaPlugin plugin) {
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferImporter importer = context.get("provider");
        var sender = context.sender();
        sender.sendMessage("Starting forced import from '" + importer.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> {
            importer.execute(ConflictPolicy.OVERWRITE);
            scheduler.runTask(plugin, () -> sender.sendMessage("Forced import from '" + importer.getName() + "' completed."));
        });
    }
}
