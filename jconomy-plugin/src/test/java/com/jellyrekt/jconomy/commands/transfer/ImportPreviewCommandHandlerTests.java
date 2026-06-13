package com.jellyrekt.jconomy.commands.transfer;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.TransferImporter;
import com.jellyrekt.jconomy.transfer.TransferPreview;

class ImportPreviewCommandHandlerTests {

    @Test
    @SuppressWarnings("unchecked")
    void execute_calls_preview_on_importer_and_sends_summary() {
        var importer = mock(TransferImporter.class);
        var preview = new TransferPreview(10, 3, 7, 2, Set.of("gold"));
        when(importer.preview()).thenReturn(preview);

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(importer);

        new ImportPreviewCommandHandler().execute(context);

        verify(importer).preview();
        verify(importer, never()).execute(any());
        verify(sender, atLeastOnce()).sendMessage(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_never_calls_execute_on_importer() {
        var importer = mock(TransferImporter.class);
        when(importer.preview()).thenReturn(new TransferPreview(0, 0, 0, 0, Set.of()));

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(importer);

        new ImportPreviewCommandHandler().execute(context);

        verify(importer, never()).execute(any());
    }
}
