package com.allaymc.landclaimremastered.service;

import com.allaymc.landclaimremastered.hooks.ClaimProviderManager;
import com.allaymc.landclaimremastered.model.Tier;
import com.allaymc.landclaimremastered.storage.PlayerProgressRepository;
import org.bukkit.entity.Player;

public final class PlayerProgressService {
    private final ClaimProviderManager claimProviderManager;
    private final TierService tierService;
    private final PlayerProgressRepository repository;

    public PlayerProgressService(
            ClaimProviderManager claimProviderManager,
            TierService tierService,
            PlayerProgressRepository repository
    ) {
        this.claimProviderManager = claimProviderManager;
        this.tierService = tierService;
        this.repository = repository;
    }

    public int totalClaimBlocks(Player player) {
        if (!claimProviderManager.isAvailable()) return 0;
        return Math.max(0, claimProviderManager.getProvider().getTotalClaimBlocks(player));
    }

    public Tier currentTier(Player player) {
        int total = totalClaimBlocks(player);
        Tier tier = tierService.resolveTier(total);
        repository.save(player.getUniqueId(), total, tier.getLevel());
        return tier;
    }

    public void sync(Player player) {
        currentTier(player);
    }
}
