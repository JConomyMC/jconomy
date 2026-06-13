package com.jellyrekt.jconomy.commands.transfer;

import static org.mockito.Mockito.*;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.TransferImporter;

class ImportListCommandHandlerTests {

    @Test
    @SuppressWarnings("unchecked")
    void execute_sends_each_importer_name_to_sender() {
        var importerA = mock(TransferImporter.class);
        var importerB = mock(TransferImporter.class);
        when(importerA.getName()).thenReturn("vault");
        when(importerB.getName()).thenReturn("essentials");

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);

        var handler = new ImportListCommandHandler(List.of(importerA, importerB));
        handler.execute(context);

        verify(sender).sendMessage(contains("vault"));
        verify(sender).sendMessage(contains("essentials"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_sends_message_when_no_importers_registered() {
        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);

        var handler = new ImportListCommandHandler(List.of());
        handler.execute(context);

        verify(sender).sendMessage(anyString());
    }
}
