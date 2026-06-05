package com.jellyrekt.jconomy.adapters;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class BukkitPlayerResolver implements PlayerResolver {

    @Override
    public OfflinePlayer resolve(String playerName) {
        var onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(p -> playerName.equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No player found with name: " + playerName));
    }
}
