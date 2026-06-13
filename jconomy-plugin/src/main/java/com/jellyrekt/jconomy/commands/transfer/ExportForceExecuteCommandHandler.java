package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferExporter;

public class ExportForceExecuteCommandHandler {

    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    public ExportForceExecuteCommandHandler(BukkitScheduler scheduler, JavaPlugin plugin) {
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferExporter exporter = context.get("provider");
        context.sender().sendMessage("Starting forced export via '" + exporter.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> exporter.execute(ConflictPolicy.OVERWRITE));
    }
}
