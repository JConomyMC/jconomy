package org.jconomy.commands.transfer;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;

import org.jconomy.transfer.ConflictPolicy;
import org.jconomy.transfer.TransferExporter;
import org.jconomy.transfer.TransferPlan;

class ExportPreviewCommandHandler {

    private final TransferPlanStore planStore;
    private final BukkitScheduler scheduler;
    private final JavaPlugin plugin;

    ExportPreviewCommandHandler(TransferPlanStore planStore, BukkitScheduler scheduler, JavaPlugin plugin) {
        this.planStore = planStore;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    public void execute(CommandContext<CommandSender> context) {
        TransferExporter exporter = context.get("provider");
        ConflictPolicy policy = context.get("policy");
        var sender = context.sender();
        sender.sendMessage("Generating export preview via '" + exporter.getName() + "'...");
        scheduler.runTaskAsynchronously(plugin, () -> {
            TransferPlan plan = exporter.createPlan(policy);
            planStore.store(sender.getName(), plan);
            scheduler.runTask(plugin, () -> sendSummary(sender, plan));
        });
    }

    private void sendSummary(CommandSender sender, TransferPlan plan) {
        sender.sendMessage("=== Export Preview ===");
        sender.sendMessage("Accounts to transfer: " + plan.accountsToTransfer().size());
        sender.sendMessage("Conflicts: " + plan.conflicts());
        sender.sendMessage("Conflict policy: " + plan.policy());
        sender.sendMessage("Run '/jconomy export " + plan.providerName() + " execute' to apply this plan.");
    }
}
