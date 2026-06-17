package org.jconomy.commands.transfer;

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

import org.jconomy.transfer.ConflictPolicy;
import org.jconomy.transfer.TransferImporter;
import org.jconomy.transfer.TransferPlan;

class ImportExecuteCommandHandlerTests {

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
        plan = new TransferPlan("my-importer", Set.of(), 0, ConflictPolicy.SKIP);
        when(context.sender()).thenReturn(sender);
        when(context.get("provider")).thenReturn(importer);
        when(importer.getName()).thenReturn("my-importer");
        when(sender.getName()).thenReturn("alice");
    }

    @Test
    void execute_sends_preview_required_message_when_no_plan_stored() {
        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(sender).sendMessage(anyString());
    }

    @Test
    void execute_does_not_dispatch_async_when_no_plan_stored() {
        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(scheduler, never()).runTaskAsynchronously(any(), any(Runnable.class));
    }

    @Test
    void execute_dispatches_import_asynchronously_when_plan_present() {
        planStore.store("alice", plan);

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void execute_sends_acknowledgement_before_async_dispatch_when_plan_present() {
        planStore.store("alice", plan);

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(sender).sendMessage(anyString());
    }

    @Test
    void execute_calls_importer_with_stored_plan() {
        planStore.store("alice", plan);
        doAnswer(inv -> {
            inv.getArgument(1, Runnable.class).run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(importer).execute(plan);
    }

    @Test
    void execute_schedules_completion_on_main_thread() {
        planStore.store("alice", plan);
        doAnswer(inv -> {
            inv.getArgument(1, Runnable.class).run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(scheduler).runTask(eq(plugin), any(Runnable.class));
    }

    @Test
    void execute_invalidates_all_plans_after_completion() {
        planStore.store("alice", plan);
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTask(eq(plugin), any(Runnable.class));

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        assertTrue(planStore.get("alice", "my-importer").isEmpty());
    }

    @Test
    void execute_sends_completion_message_after_import() {
        planStore.store("alice", plan);
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        doAnswer(inv -> { inv.getArgument(1, Runnable.class).run(); return null; })
                .when(scheduler).runTask(eq(plugin), any(Runnable.class));

        new ImportExecuteCommandHandler(planStore, scheduler, plugin).execute(context);

        verify(sender, times(2)).sendMessage(anyString());
    }
}
