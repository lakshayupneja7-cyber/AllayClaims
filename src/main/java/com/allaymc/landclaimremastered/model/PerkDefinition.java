package com.allaymc.landclaimremastered.model;

import org.bukkit.Material;

public record PerkDefinition(
        PerkKey key,
        String displayName,
        String description,
        Tier unlockTier,
        Material icon
) {
}
