package com.jellyrekt.jconomy.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.accounts.AccountAccess;
import com.jellyrekt.jconomy.config.JConomyConfig;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final AccountAccess accountAccess;
    private final JConomyConfig config;

    public PlayerJoinListener(JavaPlugin plugin, AccountAccess accountAccess, JConomyConfig config) {
        this.accountAccess = accountAccess;
        this.plugin = plugin;
        this.config = config;
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            accountAccess.getByIdAndWorld(playerId, worldName);
        });
    }
}
