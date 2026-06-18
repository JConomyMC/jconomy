package org.jconomy.listeners;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
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

import org.jconomy.accounts.Balance;
import org.jconomy.accounts.BalanceAccess;
import org.jconomy.config.CacheConfig;
import org.jconomy.config.economy.EconomyConfig;

class PlayerJoinListenerTests {

    private JavaPlugin plugin;
    private BalanceAccess balanceAccess;
    private CacheConfig cacheConfig;
    private EconomyConfig config;
    private BukkitScheduler scheduler;
    private PlayerJoinListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(JavaPlugin.class);
        balanceAccess = mock(BalanceAccess.class);
        cacheConfig = mock(CacheConfig.class);
        config = mock(EconomyConfig.class);
        scheduler = mock(BukkitScheduler.class);
        listener = new PlayerJoinListener(plugin, balanceAccess, cacheConfig, config, scheduler);
    }

    @Test
    void cacheDefaultWorldBalance_does_not_warm_when_global_toggle_disabled() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var event = new PlayerJoinEvent(player, "");

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(false);
        when(config.getDefaultWorldName()).thenReturn("world");

        listener.cacheDefaultWorldBalance(event);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(balanceAccess);
    }

    @Test
    void cacheDefaultWorldBalance_warms_only_currencies_enabled_for_join() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var event = new PlayerJoinEvent(player, "");

        var goldOptions = mock(EconomyConfig.CurrencyOptions.class);
        var goldCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(goldOptions.getCacheOptions()).thenReturn(goldCacheOptions);
        when(goldCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        var tokensOptions = mock(EconomyConfig.CurrencyOptions.class);
        var tokensCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(tokensOptions.getCacheOptions()).thenReturn(tokensCacheOptions);
        when(tokensCacheOptions.isWarmOnJoinEnabled()).thenReturn(false);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "tokens"));
        when(config.getCurrencyOptions("gold")).thenReturn(goldOptions);
        when(config.getCurrencyOptions("tokens")).thenReturn(tokensOptions);

        when(balanceAccess.get(playerId, "world", "gold")).thenReturn(Optional.of(mock(Balance.class)));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        listener.cacheDefaultWorldBalance(event);

        verify(scheduler, times(1)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(balanceAccess).get(playerId, "world", "gold");
        verify(balanceAccess, never()).get(playerId, "world", "tokens");
    }

    @Test
    void cacheDefaultWorldBalance_warms_default_and_joined_world_for_enabled_currency() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "nether");
        var event = new PlayerJoinEvent(player, "");

        var currencyOptions = mock(EconomyConfig.CurrencyOptions.class);
        var cacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(currencyOptions.getCacheOptions()).thenReturn(cacheOptions);
        when(cacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        when(config.getCurrencyOptions("gold")).thenReturn(currencyOptions);

        when(balanceAccess.get(playerId, "world", "gold")).thenReturn(Optional.of(mock(Balance.class)));
        when(balanceAccess.get(playerId, "nether", "gold")).thenReturn(Optional.of(mock(Balance.class)));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        listener.cacheDefaultWorldBalance(event);

        verify(scheduler, times(2)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(balanceAccess).get(playerId, "world", "gold");
        verify(balanceAccess).get(playerId, "nether", "gold");
    }

    @Test
    void cacheDefaultWorldBalance_schedules_async_task_via_injected_scheduler() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var options = mock(EconomyConfig.CurrencyOptions.class);
        var cacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(options.getCacheOptions()).thenReturn(cacheOptions);
        when(cacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        when(config.getCurrencyOptions("gold")).thenReturn(options);
        var event = new PlayerJoinEvent(player, "");

        listener.cacheDefaultWorldBalance(event);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
    }

    @Test
    void cacheJoinedWorldBalance_does_not_warm_for_same_world_teleport() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var options = mock(EconomyConfig.CurrencyOptions.class);
        var cacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(options.getCacheOptions()).thenReturn(cacheOptions);
        when(cacheOptions.isWarmOnTeleportEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnTeleportEnabled()).thenReturn(true);
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        when(config.getCurrencyOptions("gold")).thenReturn(options);

        var from = locationInWorld("world");
        var to = locationInWorld("world");
        var event = mock(PlayerTeleportEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);

        listener.cacheJoinedWorldBalance(event);

        verifyNoInteractions(scheduler);
        verifyNoInteractions(balanceAccess);
    }

    @Test
    void cacheJoinedWorldBalance_warms_for_cross_world_teleport() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var options = mock(EconomyConfig.CurrencyOptions.class);
        var cacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(options.getCacheOptions()).thenReturn(cacheOptions);
        when(cacheOptions.isWarmOnTeleportEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnTeleportEnabled()).thenReturn(true);
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold"));
        when(config.getCurrencyOptions("gold")).thenReturn(options);

        when(balanceAccess.get(playerId, "nether", "gold")).thenReturn(Optional.of(mock(Balance.class)));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        var from = locationInWorld("world");
        var to = locationInWorld("nether");
        var event = mock(PlayerTeleportEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getFrom()).thenReturn(from);
        when(event.getTo()).thenReturn(to);

        listener.cacheJoinedWorldBalance(event);

        verify(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(balanceAccess).get(playerId, "nether", "gold");
    }

    @Test
    void cacheDefaultWorldBalance_continues_when_balance_resolution_fails_for_one_currency() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var event = new PlayerJoinEvent(player, "");

        var goldOptions = mock(EconomyConfig.CurrencyOptions.class);
        var goldCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(goldOptions.getCacheOptions()).thenReturn(goldCacheOptions);
        when(goldCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        var tokensOptions = mock(EconomyConfig.CurrencyOptions.class);
        var tokensCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(tokensOptions.getCacheOptions()).thenReturn(tokensCacheOptions);
        when(tokensCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "tokens"));
        when(config.getCurrencyOptions("gold")).thenReturn(goldOptions);
        when(config.getCurrencyOptions("tokens")).thenReturn(tokensOptions);

        when(balanceAccess.get(playerId, "world", "gold")).thenThrow(new RuntimeException("balance resolution failure"));
        when(balanceAccess.get(playerId, "world", "tokens")).thenReturn(Optional.of(mock(Balance.class)));

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        assertDoesNotThrow(() -> listener.cacheDefaultWorldBalance(event));

        verify(scheduler, times(2)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(balanceAccess).get(playerId, "world", "gold");
        verify(balanceAccess).get(playerId, "world", "tokens");
    }

    @Test
    void cacheDefaultWorldBalance_continues_when_scheduler_submission_fails_for_one_currency() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var event = new PlayerJoinEvent(player, "");
        var account = mock(Account.class);

        var goldOptions = mock(EconomyConfig.CurrencyOptions.class);
        var goldCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(goldOptions.getCacheOptions()).thenReturn(goldCacheOptions);
        when(goldCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        var tokensOptions = mock(EconomyConfig.CurrencyOptions.class);
        var tokensCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(tokensOptions.getCacheOptions()).thenReturn(tokensCacheOptions);
        when(tokensCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "tokens"));
        when(config.getCurrencyOptions("gold")).thenReturn(goldOptions);
        when(config.getCurrencyOptions("tokens")).thenReturn(tokensOptions);
        when(accountAccess.getByIdAndWorld(playerId, "world")).thenReturn(Optional.of(account));

        final int[] callCount = { 0 };
        doAnswer(invocation -> {
            callCount[0]++;
            if (callCount[0] == 1) {
                throw new RuntimeException("scheduler failure");
            }
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        assertDoesNotThrow(() -> listener.cacheDefaultWorldBalance(event));

        verify(scheduler, times(2)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(account, times(1)).getBalance(anyString());
    }

    @Test
    void cacheDefaultWorldBalance_continues_when_balance_resolution_fails_for_one_currency() {
        var playerId = UUID.randomUUID();
        var player = playerInWorld(playerId, "world");
        var event = new PlayerJoinEvent(player, "");
        var account = mock(Account.class);

        var goldOptions = mock(EconomyConfig.CurrencyOptions.class);
        var goldCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(goldOptions.getCacheOptions()).thenReturn(goldCacheOptions);
        when(goldCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        var tokensOptions = mock(EconomyConfig.CurrencyOptions.class);
        var tokensCacheOptions = mock(EconomyConfig.CurrencyOptions.CurrencyCacheOptions.class);
        when(tokensOptions.getCacheOptions()).thenReturn(tokensCacheOptions);
        when(tokensCacheOptions.isWarmOnJoinEnabled()).thenReturn(true);

        when(cacheConfig.isWarmOnJoinEnabled()).thenReturn(true);
        when(config.getDefaultWorldName()).thenReturn("world");
        when(config.getAllCurrencyNames()).thenReturn(Set.of("gold", "tokens"));
        when(config.getCurrencyOptions("gold")).thenReturn(goldOptions);
        when(config.getCurrencyOptions("tokens")).thenReturn(tokensOptions);
        when(accountAccess.getByIdAndWorld(playerId, "world")).thenReturn(Optional.of(account));

        doThrow(new RuntimeException("balance resolution failure")).when(account).getBalance("gold");

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return null;
        }).when(scheduler).runTaskAsynchronously(eq(plugin), any(Runnable.class));

        assertDoesNotThrow(() -> listener.cacheDefaultWorldBalance(event));

        verify(scheduler, times(2)).runTaskAsynchronously(eq(plugin), any(Runnable.class));
        verify(account).getBalance("gold");
        verify(account).getBalance("tokens");
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
