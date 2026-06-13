package com.jellyrekt.jconomy.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public class CommandManagerFactory {

    private final JavaPlugin plugin;

    public CommandManagerFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public LegacyPaperCommandManager<CommandSender> create() {
        LegacyPaperCommandManager<CommandSender> manager;
        try {
            manager = LegacyPaperCommandManager.createNative(plugin, ExecutionCoordinator.simpleCoordinator());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create command manager", e);
        }
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        return manager;
    }
}
