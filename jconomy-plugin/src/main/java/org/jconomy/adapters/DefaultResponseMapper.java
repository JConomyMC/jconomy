package org.jconomy.adapters;

import net.milkbowl.vault2.economy.EconomyResponse;
import net.milkbowl.vault2.economy.EconomyResponse.ResponseType;

public class DefaultResponseMapper implements EconomyResponseMapper {

    @Override
    public net.milkbowl.vault.economy.EconomyResponse getLegacyResponse(EconomyResponse modernResponse) {
        return new net.milkbowl.vault.economy.EconomyResponse(
                modernResponse.amount.doubleValue(),
                modernResponse.balance.doubleValue(),
                getLegacyResponseType(modernResponse.type),
                modernResponse.errorMessage);
    }

    private static net.milkbowl.vault.economy.EconomyResponse.ResponseType getLegacyResponseType(ResponseType modernResponse) {
        switch (modernResponse) {
            case SUCCESS:
                return net.milkbowl.vault.economy.EconomyResponse.ResponseType.SUCCESS;
            case FAILURE:
                return net.milkbowl.vault.economy.EconomyResponse.ResponseType.FAILURE;
            case NOT_IMPLEMENTED:
                return net.milkbowl.vault.economy.EconomyResponse.ResponseType.NOT_IMPLEMENTED;
            default:
                throw new IllegalArgumentException("Unknown modern ResponseType: " + modernResponse);
        }
    }
}