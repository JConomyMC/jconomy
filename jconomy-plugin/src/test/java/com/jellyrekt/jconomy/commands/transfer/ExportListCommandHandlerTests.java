package com.jellyrekt.jconomy.commands.transfer;

import static org.mockito.Mockito.*;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.TransferExporter;

class ExportListCommandHandlerTests {

    @Test
    @SuppressWarnings("unchecked")
    void execute_sends_each_exporter_name_to_sender() {
        var exporterA = mock(TransferExporter.class);
        var exporterB = mock(TransferExporter.class);
        when(exporterA.getName()).thenReturn("vault");
        when(exporterB.getName()).thenReturn("essentials");

        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);

        var handler = new ExportListCommandHandler(List.of(exporterA, exporterB));
        handler.execute(context);

        verify(sender).sendMessage(contains("vault"));
        verify(sender).sendMessage(contains("essentials"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_sends_message_when_no_exporters_registered() {
        var sender = mock(CommandSender.class);
        var context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);

        var handler = new ExportListCommandHandler(List.of());
        handler.execute(context);

        verify(sender).sendMessage(anyString());
    }
}
