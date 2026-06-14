package org.jconomy.listeners;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jconomy.accounts.AccountAccess;
import org.jconomy.config.economy.EconomyConfig;

class PlayerJoinListenerTests {

    private JavaPlugin plugin;
    private AccountAccess accountAccess;
    private EconomyConfig config;
    private BukkitScheduler scheduler;
    private PlayerJoinListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        accountAccess = mock(AccountAccess.class);
        config = mock(EconomyConfig.class);
        scheduler = mock(BukkitScheduler.class);
        listener = new PlayerJoinListener(plugin, accountAccess, config, scheduler);
    }

    @Test
    void cacheDefaultWorldBalance_schedules_async_task_via_injected_scheduler() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        when(config.getDefaultWorldName()).thenReturn("world");
        var event = new PlayerJoinEvent(player, "");

        listener.cacheDefaultWorldBalance(event);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void cacheJoinedWorldBalance_schedules_async_task_via_injected_scheduler() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "nether");
        var event = mock(PlayerTeleportEvent.class);
        var destination = locationInWorld("nether");
        when(event.getPlayer()).thenReturn(player);
        when(event.getTo()).thenReturn(destination);

        listener.cacheJoinedWorldBalance(event);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    // --- Helpers ---

    private static Player playerInWorld(UUID id, String worldName) {
        var world = mock(World.class);
        when(world.getName()).thenReturn(worldName);
        var location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(id);
        when(player.getLocation()).thenReturn(location);
        return player;
    }

    private static Location locationInWorld(String worldName) {
        var world = mock(World.class);
        when(world.getName()).thenReturn(worldName);
        var location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);
        return location;
    }
}
