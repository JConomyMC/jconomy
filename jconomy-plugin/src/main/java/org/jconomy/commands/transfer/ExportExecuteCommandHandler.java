package org.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.transfer.TransferExporter;

class ExportExecuteCommandHandler {

    private final TransferPlanStore planStore;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    ExportExecuteCommandHandler(TransferPlanStore planStore, BukkitScheduler scheduler, JavaPlugin plugin) {
        this.planStore = planStore;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferExporter exporter = context.get("provider");
        var sender = context.sender();
        var plan = planStore.get(sender.getName(), exporter.getName());
        if (plan.isEmpty()) {
            sender.sendMessage("No preview found for '" + exporter.getName() + "'. Run: /jconomy export " + exporter.getName() + " preview");
            return;
        }
        var resolvedPlan = plan.get();
        sender.sendMessage("Starting export via '" + exporter.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> {
            exporter.execute(resolvedPlan);
            scheduler.runTask(plugin, () -> {
                planStore.invalidateAll();
                sender.sendMessage("Export via '" + exporter.getName() + "' completed.");
            });
        });
    }
}
