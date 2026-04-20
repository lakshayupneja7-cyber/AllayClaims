package com.allaymc.landclaimremastered.placeholders;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.model.PerkKey;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class AllayClaimsExpansion extends PlaceholderExpansion {

    private final AllayClaimsPlugin plugin;

    public AllayClaimsExpansion(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "allayclaim";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AllayMc";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        return switch (params.toLowerCase()) {
            case "active_perk" -> activePerk(player);
            case "tier" -> plugin.getPlayerProgressService().currentTier(player).name();
            case "blocks" -> String.valueOf(plugin.getPlayerProgressService().totalClaimBlocks(player));
            case "claim_id" -> currentClaim(player).map(ClaimContext::claimId).orElse("None");
            case "claim_name" -> currentClaimProfile(player).map(ClaimProfile::getDisplayName).orElse("None");
            case "trust_mode" -> currentClaimProfile(player).map(profile -> profile.getTrustMode().name()).orElse("None");
            default -> "";
        };
    }

    private String activePerk(Player player) {
        Optional<PerkKey> perkKey = plugin.getPerkService().activePerk(player);
        if (perkKey.isEmpty()) {
            return "None";
        }

        Optional<PerkDefinition> def = plugin.getPerkService().getDefinition(perkKey.get());
        return def.map(PerkDefinition::displayName).orElse(perkKey.get().name());
    }

    private Optional<ClaimContext> currentClaim(Player player) {
        return plugin.getPerkService().currentClaim(player);
    }

    private Optional<ClaimProfile> currentClaimProfile(Player player) {
        Optional<ClaimContext> context = currentClaim(player);
        if (context.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(plugin.getClaimProfileService().getOrCreate(
                context.get().claimId(),
                context.get().owner()
        ));
    }
}
