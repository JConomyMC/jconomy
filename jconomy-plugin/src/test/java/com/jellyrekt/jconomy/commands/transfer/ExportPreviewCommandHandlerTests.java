package com.jellyrekt.jconomy.commands.transfer;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.TransferExporter;
import com.jellyrekt.jconomy.transfer.TransferPreview;

class ExportPreviewCommandHandlerTests {

    @Test
    @SuppressWarnings("unchecked")
    void execute_calls_preview_on_exporter_and_sends_summary() {
        var exporter = mock(TransferExporter.class);
        var preview = new TransferPreview(10, 3, 7, 2, Set.of("gold"));
        when(exporter.preview()).thenReturn(preview);

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(exporter);

        new ExportPreviewCommandHandler().execute(context);

        verify(exporter).preview();
        verify(exporter, never()).execute(any());
        verify(sender, atLeastOnce()).sendMessage(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_never_calls_execute_on_exporter() {
        var exporter = mock(TransferExporter.class);
        when(exporter.preview()).thenReturn(new TransferPreview(0, 0, 0, 0, Set.of()));

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(exporter);

        new ExportPreviewCommandHandler().execute(context);

        verify(exporter, never()).execute(any());
    }
}
