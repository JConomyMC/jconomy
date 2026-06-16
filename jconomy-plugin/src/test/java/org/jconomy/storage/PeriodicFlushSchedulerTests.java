package org.jconomy.storage;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jconomy.config.CacheConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PeriodicFlushSchedulerTests {

    private JavaPlugin plugin;
    private BukkitScheduler scheduler;
    private FlushRegistry registry;
    private CacheConfig.PeriodicFlushConfig periodicFlushConfig;
    private BukkitTask task;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        scheduler = mock(BukkitScheduler.class);
        registry = mock(FlushRegistry.class);
        periodicFlushConfig = mock(CacheConfig.PeriodicFlushConfig.class);
        task = mock(BukkitTask.class);

        when(periodicFlushConfig.getIntervalTicks()).thenReturn(1200);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), anyLong(), anyLong())).thenReturn(task);
    }

    @Test
    void start_schedules_repeating_task_with_configured_interval() {
        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).start();

        verify(scheduler).runTaskTimer(eq(plugin), any(Runnable.class), eq(1200L), eq(1200L));
    }

    @Test
    void timer_trigger_dispatches_async_flush_when_not_already_flushing() {
        var timerBody = ArgumentCaptor.forClass(Runnable.class);
        when(scheduler.runTaskTimer(eq(plugin), timerBody.capture(), anyLong(), anyLong())).thenReturn(task);

        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).start();
        timerBody.getValue().run();

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void timer_trigger_skips_async_flush_when_previous_flush_still_running() {
        var timerBody = ArgumentCaptor.forClass(Runnable.class);
        when(scheduler.runTaskTimer(eq(plugin), timerBody.capture(), anyLong(), anyLong())).thenReturn(task);
        // Don't run the async body, so flushing stays true after first trigger
        when(scheduler.runTaskAsynchronously(eq(plugin), any(Runnable.class))).thenReturn(task);

        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).start();
        timerBody.getValue().run(); // flushing -> true, dispatches async (not yet completed)
        timerBody.getValue().run(); // flushing still true, should skip

        verify(scheduler, times(1)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void async_flush_calls_flush_registry() {
        var timerBody = ArgumentCaptor.forClass(Runnable.class);
        when(scheduler.runTaskTimer(eq(plugin), timerBody.capture(), anyLong(), anyLong())).thenReturn(task);
        var asyncBody = ArgumentCaptor.forClass(Runnable.class);
        when(scheduler.runTaskAsynchronously(eq(plugin), asyncBody.capture())).thenReturn(task);

        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).start();
        timerBody.getValue().run();
        asyncBody.getValue().run();

        verify(registry).flushAll();
    }

    @Test
    void async_flush_resets_flushing_flag_so_next_trigger_dispatches_again() {
        var timerBody = ArgumentCaptor.forClass(Runnable.class);
        when(scheduler.runTaskTimer(eq(plugin), timerBody.capture(), anyLong(), anyLong())).thenReturn(task);
        // Run the async body immediately so flushing is reset before second trigger
        doAnswer(inv -> {
            inv.getArgument(1, Runnable.class).run();
            return task;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).start();
        timerBody.getValue().run(); // dispatches and completes async: flushing -> false
        timerBody.getValue().run(); // should dispatch again

        verify(scheduler, times(2)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void stop_cancels_the_scheduled_task() {
        var flushScheduler = new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig);
        flushScheduler.start();

        flushScheduler.stop();

        verify(task).cancel();
    }

    @Test
    void stop_before_start_does_not_throw() {
        new PeriodicFlushScheduler(plugin, scheduler, registry, periodicFlushConfig).stop();
    }
}
