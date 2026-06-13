package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferImporter;

public class ImportExecuteCommandHandler {

    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    public ImportExecuteCommandHandler(BukkitScheduler scheduler, JavaPlugin plugin) {
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferImporter importer = context.get("provider");
        context.sender().sendMessage("Starting import from '" + importer.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> importer.execute(ConflictPolicy.SKIP));
    }
}
