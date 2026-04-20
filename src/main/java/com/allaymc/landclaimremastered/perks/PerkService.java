package com.allaymc.landclaimremastered.perks;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.config.PluginConfig;
import com.allaymc.landclaimremastered.hooks.ClaimProviderManager;
import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.model.PerkKey;
import com.allaymc.landclaimremastered.model.ClaimTrustMode;
import com.allaymc.landclaimremastered.model.Tier;
import com.allaymc.landclaimremastered.service.ClaimProfileService;
import com.allaymc.landclaimremastered.service.PlayerProgressService;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class PerkService {
    private final AllayClaimsPlugin plugin;
    private final ClaimProviderManager claimProviderManager;
    private final ClaimProfileService claimProfileService;
    private final PlayerProgressService playerProgressService;
    private final PerkRegistry perkRegistry;
    private final PluginConfig config;

    public PerkService(
            AllayClaimsPlugin plugin,
            ClaimProviderManager claimProviderManager,
            ClaimProfileService claimProfileService,
            PlayerProgressService playerProgressService,
            PerkRegistry perkRegistry,
            PluginConfig config
    ) {
        this.plugin = plugin;
        this.claimProviderManager = claimProviderManager;
        this.claimProfileService = claimProfileService;
        this.playerProgressService = playerProgressService;
        this.perkRegistry = perkRegistry;
        this.config = config;
    }

    public Collection<PerkDefinition> allPerks() { return perkRegistry.all(); }
    public Optional<PerkDefinition> getDefinition(PerkKey key) { return perkRegistry.find(key); }

    public Optional<ClaimContext> currentClaim(Player player) {
        if (!claimProviderManager.isAvailable()) return Optional.empty();
        return claimProviderManager.getProvider().getClaimAt(player.getLocation());
    }

    public boolean isUnlocked(Player player, PerkKey key) {
        Tier tier = playerProgressService.currentTier(player);
        return perkRegistry.find(key)
                .map(def -> tier.getLevel() >= def.unlockTier().getLevel())
                .orElse(false);
    }

    public Optional<PerkKey> activePerk(Player player) {
        Optional<ClaimContext> claimOptional = currentClaim(player);
        if (claimOptional.isEmpty()) return Optional.empty();

        ClaimContext context = claimOptional.get();
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());

        if (profile.getSelectedPerk() == null) return Optional.empty();
        if (!canReceive(player.getUniqueId(), context, profile)) return Optional.empty();
        if (!isUnlocked(player, profile.getSelectedPerk())) return Optional.empty();
        return Optional.of(profile.getSelectedPerk());
    }

    private boolean canReceive(UUID uuid, ClaimContext context, ClaimProfile profile) {
        ClaimTrustMode mode = profile.getTrustMode();
        return switch (mode) {
            case OWNER_ONLY -> context.owner().equals(uuid);
            case ALL_TRUSTED -> context.isTrusted(uuid);
            case WHITELIST_ONLY -> context.owner().equals(uuid) || profile.getPerkWhitelist().contains(uuid);
        };
    }

    public void applyCurrentClaimPerk(Player player) {
        refreshPerk(player);
    }

    public void refreshPerk(Player player) {
        clearPotionPerks(player);
        Optional<PerkKey> perkOptional = activePerk(player);
        if (perkOptional.isEmpty()) return;

        switch (perkOptional.get()) {
            case SKYBOUND -> player.addPotionEffect(effect(PotionEffectType.JUMP_BOOST, 0));
            case TRAILBLAZER -> {
                player.addPotionEffect(effect(PotionEffectType.SPEED, 0));
                player.addPotionEffect(effect(PotionEffectType.JUMP_BOOST, 0));
            }
            case STONEHEART -> player.addPotionEffect(effect(PotionEffectType.RESISTANCE, 0));
            case DEEP_FOCUS -> player.addPotionEffect(effect(PotionEffectType.HASTE, 0));
            case WINDSTEP -> player.addPotionEffect(effect(PotionEffectType.SPEED, 0));
            case MOONSIGHT -> player.addPotionEffect(effect(PotionEffectType.NIGHT_VISION, 0));
            case BUILDERS_GRACE -> player.addPotionEffect(effect(PotionEffectType.HASTE, 0));
            case HEARTHLIGHT -> player.addPotionEffect(effect(PotionEffectType.REGENERATION, 0));
            case STORMSTRIDE -> player.addPotionEffect(effect(PotionEffectType.SPEED, 1));
            case TITAN_BLOOD -> player.addPotionEffect(effect(PotionEffectType.STRENGTH, 0));
            case EVERGLOW -> {
                player.addPotionEffect(effect(PotionEffectType.REGENERATION, 0));
                player.addPotionEffect(effect(PotionEffectType.NIGHT_VISION, 0));
            }
            default -> {}
        }
    }

    private PotionEffect effect(PotionEffectType type, int amplifier) {
        return new PotionEffect(type, config.perkRefreshTicks() + 20, amplifier, true, false, true);
    }

    public boolean handleFoodPerks(Player player) {
        return activePerk(player).filter(p -> p == PerkKey.HEARTHWARMTH).isPresent();
    }

    public boolean handleFallPerks(Player player) {
        return activePerk(player).filter(p -> p == PerkKey.FEATHERFALL_WARD).isPresent();
    }

    public boolean handleSmeltPerk(Player player) {
        return activePerk(player).filter(p -> p == PerkKey.IRON_RHYTHM).isPresent();
    }

    public boolean handleCropPerk(Player player) {
        return activePerk(player).filter(p -> p == PerkKey.VERDANT_PULSE).isPresent();
    }

    public void clearPotionPerks(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.HASTE);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.STRENGTH);
    }
}
