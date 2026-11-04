package org.jconomy.storage;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import org.jconomy.config.CacheConfig;

public class PeriodicFlushScheduler {
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final FlushRegistry registry;
    private final CacheConfig.PeriodicFlushConfig config;
    private volatile CompletableFuture<Void> activeFlush = CompletableFuture.completedFuture(null);

    private BukkitTask task;

    public PeriodicFlushScheduler(
            JavaPlugin plugin,
            BukkitScheduler scheduler,
            FlushRegistry registry,
            CacheConfig.PeriodicFlushConfig config) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.registry = registry;
        this.config = config;
    }

    public void start() {
        long interval = config.getIntervalTicks();
        task = scheduler.runTaskTimer(plugin, () -> {
            if (activeFlush.isDone()) {
                var flush = new CompletableFuture<Void>();
                activeFlush = flush;
                scheduler.runTaskAsynchronously(plugin, () -> {
                    try {
                        registry.flushAll();
                    } finally {
                        flush.complete(null);
                    }
                });
            }
        }, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public boolean awaitIdle(Duration timeout) {
        try {
            activeFlush.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException | ExecutionException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
