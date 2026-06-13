package com.jellyrekt.jconomy.commands.transfer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferExporter;

class ExportExecuteCommandHandlerTests {

    private TransferExporter exporter;
    private BukkitScheduler scheduler;
    private JavaPlugin plugin;
    private CommandSender sender;
    private CommandContext<CommandSender> context;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        exporter = mock(TransferExporter.class);
        scheduler = mock(BukkitScheduler.class);
        plugin = mock(JavaPlugin.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(exporter);
    }

    @Test
    void execute_dispatches_export_asynchronously() {
        new ExportExecuteCommandHandler(scheduler, plugin).execute(context);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void execute_sends_acknowledgement_before_async_dispatch() {
        new ExportExecuteCommandHandler(scheduler, plugin).execute(context);

        verify(sender).sendMessage(anyString());
    }

    @Test
    void execute_calls_exporter_with_skip_policy() {
        doAnswer(inv -> {
            inv.getArgument(1, Runnable.class).run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ExportExecuteCommandHandler(scheduler, plugin).execute(context);

        verify(exporter).execute(ConflictPolicy.SKIP);
    }

    @Test
    void force_execute_calls_exporter_with_overwrite_policy() {
        doAnswer(inv -> {
            inv.getArgument(1, Runnable.class).run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ExportForceExecuteCommandHandler(scheduler, plugin).execute(context);

        verify(exporter).execute(ConflictPolicy.OVERWRITE);
    }

    @Test
    void force_execute_dispatches_asynchronously() {
        new ExportForceExecuteCommandHandler(scheduler, plugin).execute(context);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }
}
