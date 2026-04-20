package com.allaymc.landclaimremastered.hooks;

import com.allaymc.landclaimremastered.model.ClaimContext;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface ClaimProvider {
    String getName();
    boolean isAvailable();
    Optional<ClaimContext> getClaimAt(Location location);
    int getTotalClaimBlocks(Player player);
}
