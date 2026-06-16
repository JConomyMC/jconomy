package org.jconomy.storage;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import org.jconomy.config.CacheConfig;

public class PeriodicFlushScheduler {
    private final JavaPlugin plugin;
    private final BukkitScheduler scheduler;
    private final FlushRegistry registry;
    private final CacheConfig.PeriodicFlushConfig config;
    private final AtomicBoolean flushing = new AtomicBoolean(false);

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
            if (flushing.compareAndSet(false, true)) {
                scheduler.runTaskAsynchronously(plugin, () -> {
                    try {
                        registry.flushAll();
                    } finally {
                        flushing.set(false);
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
}
