package com.jellyrekt.jconomy.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jellyrekt.jconomy.balances.BalanceRepository;
import com.jellyrekt.jconomy.config.JConomyConfig;

public class PlayerJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final JConomyConfig config;
    private final BalanceRepository balanceRepository;

    public PlayerJoinListener(JavaPlugin plugin, JConomyConfig config, BalanceRepository balanceRepository) {
        this.plugin = plugin;
        this.config = config;
        this.balanceRepository = balanceRepository;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        for (var currencyName : config.getAllCurrencyNames()) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                createBalanceIfNotExists(playerId, currencyName);
            });
        }
    }

    private void createBalanceIfNotExists(UUID playerId, String currencyName) {
        var balance = balanceRepository.getByPlayerIdAndCurrencyName(playerId, currencyName);
        
        if (balance.isEmpty()) {
            balanceRepository.set(playerId, currencyName, 0);
        }
    }
}
