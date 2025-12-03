package com.jellyrekt.jconomy.balances;

import java.util.UUID;

public record Balance(UUID playerId, String currencyName, double amount) { }
