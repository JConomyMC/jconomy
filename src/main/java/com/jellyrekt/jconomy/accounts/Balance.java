package com.jellyrekt.jconomy.accounts;

import java.util.UUID;

public record Balance(UUID playerId, String currencyName, double amount) { }
