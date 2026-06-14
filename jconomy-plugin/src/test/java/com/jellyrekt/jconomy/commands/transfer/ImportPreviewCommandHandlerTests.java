package com.jellyrekt.jconomy.commands.transfer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.incendo.cloud.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jellyrekt.jconomy.transfer.ConflictPolicy;
import com.jellyrekt.jconomy.transfer.TransferImporter;
import com.jellyrekt.jconomy.transfer.TransferPlan;

class ImportPreviewCommandHandlerTests {

    private TransferImporter importer;
    private BukkitScheduler scheduler;
    private JavaPlugin plugin;
    private CommandSender sender;
    private CommandContext<CommandSender> context;
    private TransferPlanStore planStore;
    private TransferPlan plan;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        importer = mock(TransferImporter.class);
        scheduler = mock(BukkitScheduler.class);
        plugin = mock(JavaPlugin.class);
        sender = mock(CommandSender.class);
        context = mock(CommandContext.class);
        planStore = new TransferPlanStore();
        plan = new TransferPlan("my-importer", Set.of(), Set.of(), 0, ConflictPolicy.SKIP);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(importer);
        when(context.get("policy")).thenReturn(ConflictPolicy.SKIP);
        when(importer.getName()).thenReturn("my-importer");
        when(importer.preview(ConflictPolicy.SKIP)).thenReturn(plan);
        when(sender.getName()).thenReturn("alice");
    }

    @Test
    void execute_dispatches_preview_asynchronously() {
        new ImportPreviewCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void execute_calls_preview_with_policy_from_context() {
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportPreviewCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(importer).preview(ConflictPolicy.SKIP);
    }

    @Test
    void execute_stores_plan_in_plan_store() {
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportPreviewCommandHandler(planStore, scheduler, plugin).execute(context);

        assertTrue(planStore.get("alice", "my-importer").isPresent());
    }

    @Test
    void execute_sends_summary_on_main_thread() {
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportPreviewCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    @Test
    void execute_never_calls_execute_on_importer() {
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportPreviewCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(importer, never()).execute(any());
    }
}
