package com.jellyrekt.jconomy.commands.transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.jellyrekt.jconomy.transfer.TransferPlan;

public class TransferPlanStore {

    private final Map<String, Map<String, TransferPlan>> plans = new HashMap<>();

    void store(String senderName, TransferPlan plan) {
        plans.computeIfAbsent(senderName, k -> new HashMap<>()).put(plan.providerName(), plan);
    }

    Optional<TransferPlan> get(String senderName, String providerName) {
        var senderPlans = plans.get(senderName);
        if (senderPlans == null) return Optional.empty();
        return Optional.ofNullable(senderPlans.get(providerName));
    }

    void invalidateAll() {
        plans.clear();
    }
}
