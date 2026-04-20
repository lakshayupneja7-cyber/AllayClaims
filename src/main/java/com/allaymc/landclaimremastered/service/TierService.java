package com.allaymc.landclaimremastered.service;

import com.allaymc.landclaimremastered.config.PluginConfig;
import com.allaymc.landclaimremastered.model.Tier;

public final class TierService {
    private final PluginConfig config;

    public TierService(PluginConfig config) {
        this.config = config;
    }

    public int requiredBlocks(Tier tier) {
        return config.requiredBlocks(tier);
    }

    public Tier resolveTier(int totalClaimBlocks) {
        Tier result = Tier.I;
        for (Tier tier : Tier.values()) {
            if (totalClaimBlocks >= requiredBlocks(tier)) {
                result = tier;
            }
        }
        return result;
    }
}
