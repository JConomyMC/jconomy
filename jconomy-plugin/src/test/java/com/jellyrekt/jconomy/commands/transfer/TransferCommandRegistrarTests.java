package com.jellyrekt.jconomy.commands.transfer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.CommandManager;
import org.junit.jupiter.api.Test;

class TransferCommandRegistrarTests {

    @Test
    @SuppressWarnings("unchecked")
    void register_does_not_throw_with_empty_provider_lists() {
        CommandManager<CommandSender> commandManager = mock(CommandManager.class);
        var registrar = new TransferCommandRegistrar(commandManager, List.of(), List.of());

        assertDoesNotThrow(() -> registrar.register());
    }
}
