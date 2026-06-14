package org.jconomy.adapters;

import net.milkbowl.vault2.economy.EconomyResponse;

public interface EconomyResponseMapper {
    public net.milkbowl.vault.economy.EconomyResponse getLegacyResponse(EconomyResponse modernResponse);
}
