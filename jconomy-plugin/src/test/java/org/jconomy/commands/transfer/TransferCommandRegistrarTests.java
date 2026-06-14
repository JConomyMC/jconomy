package org.jconomy.commands.transfer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.CommandManager;
import org.junit.jupiter.api.Test;

class TransferCommandRegistrarTests {

    @Test
    @SuppressWarnings("unchecked")
    void register_does_not_throw_with_empty_provider_lists() {
        CommandManager<CommandSender> commandManager = mock(CommandManager.class, RETURNS_DEEP_STUBS);
        var scheduler = mock(BukkitScheduler.class);
        var plugin = mock(JavaPlugin.class);
        var planStore = new TransferPlanStore();
        var registrar = new TransferCommandRegistrar(commandManager, List.of(), List.of(), scheduler, plugin, planStore);

        assertDoesNotThrow(() -> registrar.register());
    }
}
