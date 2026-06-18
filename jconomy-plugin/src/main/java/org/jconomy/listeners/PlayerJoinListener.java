package org.jconomy.listeners;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import org.jconomy.accounts.BalanceAccess;
import org.jconomy.config.CacheConfig;
import org.jconomy.config.economy.EconomyConfig;

public class PlayerJoinListener implements Listener {
    private static final Logger logger = Logger.getLogger(PlayerJoinListener.class.getName());

    private final JavaPlugin plugin;
    private final BalanceAccess balanceAccess;
    private final CacheConfig cacheConfig;
    private final EconomyConfig config;
    private final BukkitScheduler scheduler;

    public PlayerJoinListener(JavaPlugin plugin, BalanceAccess balanceAccess, CacheConfig cacheConfig, EconomyConfig config,
            BukkitScheduler scheduler) {
        this.balanceAccess = balanceAccess;
        this.cacheConfig = cacheConfig;
        this.plugin = plugin;
        this.config = config;
        this.scheduler = scheduler;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void cacheDefaultWorldBalance(PlayerJoinEvent event) {
        if (!cacheConfig.isWarmOnJoinEnabled()) {
            return;
        }

        var playerId = event.getPlayer().getUniqueId();
        var defaultWorldName = config.getDefaultWorldName();
        warmCurrencyBalances(playerId, defaultWorldName);

        var currentWorldName = event.getPlayer().getLocation().getWorld().getName();

        if (!defaultWorldName.equals(currentWorldName)) {
            warmCurrencyBalances(playerId, currentWorldName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void cacheJoinedWorldBalance(PlayerTeleportEvent event) {
        if (!cacheConfig.isWarmOnTeleportEnabled()) {
            return;
        }

        var fromWorldName = getWorldName(event.getFrom());
        var toWorldName = getWorldName(event.getTo());

        if (toWorldName == null) {
            return;
        }

        if (toWorldName.equals(fromWorldName)) {
            return;
        }

        warmCurrencyBalancesOnTeleport(event.getPlayer().getUniqueId(), toWorldName);
    }

    private void warmCurrencyBalances(UUID playerId, String worldName) {
        config.getAllCurrencyNames().stream()
                .filter(this::isJoinWarmingEnabledForCurrency)
                .forEach(currencyName -> scheduleCache(playerId, worldName, currencyName));
    }

    private void warmCurrencyBalancesOnTeleport(UUID playerId, String worldName) {
        config.getAllCurrencyNames().stream()
                .filter(this::isTeleportWarmingEnabledForCurrency)
                .forEach(currencyName -> scheduleCache(playerId, worldName, currencyName));
    }

    private boolean isJoinWarmingEnabledForCurrency(String currencyName) {
        var currencyOptions = config.getCurrencyOptions(currencyName);
        return currencyOptions != null
                && currencyOptions.getCacheOptions() != null
                && currencyOptions.getCacheOptions().isWarmOnJoinEnabled();
    }

    private boolean isTeleportWarmingEnabledForCurrency(String currencyName) {
        var currencyOptions = config.getCurrencyOptions(currencyName);
        return currencyOptions != null
                && currencyOptions.getCacheOptions() != null
                && currencyOptions.getCacheOptions().isWarmOnTeleportEnabled();
    }

    private void scheduleCache(UUID playerId, String worldName, String currencyName) {
        try {
            scheduler.runTaskAsynchronously(plugin, () -> {
                try {
                    balanceAccess.get(playerId, worldName, currencyName);
                } catch (Exception e) {
                    logger.log(Level.WARNING,
                            String.format("Failed to warm cache for player %s, world %s, currency %s",
                                    playerId, worldName, currencyName),
                            e);
                }
            });
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    String.format("Failed to schedule cache warming for player %s, world %s, currency %s",
                            playerId, worldName, currencyName),
                    e);
        }
    }

    private String getWorldName(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return location.getWorld().getName();
    }
}
