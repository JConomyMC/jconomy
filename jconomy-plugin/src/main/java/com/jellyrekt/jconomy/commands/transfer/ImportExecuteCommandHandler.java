package com.jellyrekt.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import com.jellyrekt.jconomy.transfer.TransferImporter;

class ImportExecuteCommandHandler {

    private final TransferPlanStore planStore;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    ImportExecuteCommandHandler(TransferPlanStore planStore, BukkitScheduler scheduler, JavaPlugin plugin) {
        this.planStore = planStore;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferImporter importer = context.get("provider");
        var sender = context.sender();
        var plan = planStore.get(sender.getName(), importer.getName());
        if (plan.isEmpty()) {
            sender.sendMessage("No preview found for '" + importer.getName() + "'. Run: /jconomy import " + importer.getName() + " preview");
            return;
        }
        var resolvedPlan = plan.get();
        sender.sendMessage("Starting import from '" + importer.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> {
            importer.execute(resolvedPlan);
            scheduler.runTask(plugin, () -> {
                planStore.invalidateAll();
                sender.sendMessage("Import from '" + importer.getName() + "' completed.");
            });
        });
    }
}
