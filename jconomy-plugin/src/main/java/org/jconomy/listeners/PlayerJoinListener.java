package org.jconomy.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.config.economy.EconomyConfig;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final AccountAccess accountAccess;
    private final EconomyConfig config;
    private final BukkitScheduler scheduler;

    public PlayerJoinListener(JavaPlugin plugin, AccountAccess accountAccess, EconomyConfig config,
            BukkitScheduler scheduler) {
        this.accountAccess = accountAccess;
        this.plugin = plugin;
        this.config = config;
        this.scheduler = scheduler;
    }

    @EventHandler
    public void cacheDefaultWorldBalance(PlayerJoinEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        scheduleCache(playerId, config.getDefaultWorldName());

        var currentWorldName = event.getPlayer().getLocation().getWorld().getName();

        if (!config.getDefaultWorldName().equals(currentWorldName)) {
            scheduleCache(playerId, currentWorldName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void cacheJoinedWorldBalance(PlayerTeleportEvent event) {
        scheduleCache(event.getPlayer().getUniqueId(), event.getTo().getWorld().getName());
    }

    private void scheduleCache(UUID playerId, String worldName) {
        scheduler.runTaskAsynchronously(plugin, () -> {
            accountAccess.getByIdAndWorld(playerId, worldName);
        });
    }
}
