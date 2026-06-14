package org.jconomy.adapters;

import org.bukkit.OfflinePlayer;

public interface PlayerResolver {
    OfflinePlayer resolve(String playerName);
}
