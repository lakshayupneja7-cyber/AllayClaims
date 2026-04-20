package com.allaymc.landclaimremastered.listeners;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.util.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PlayerListener implements Listener {
    private final AllayClaimsPlugin plugin;
    private final Map<java.util.UUID, String> lastClaim = new HashMap<>();

    public PlayerListener(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerProgressService().sync(event.getPlayer());
        plugin.getPerkService().refreshPerk(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        Optional<ClaimContext> context = plugin.getPerkService().currentClaim(player);
        String newClaim = context.map(ClaimContext::claimId).orElse("NONE");
        String oldClaim = lastClaim.getOrDefault(player.getUniqueId(), "NONE");
        lastClaim.put(player.getUniqueId(), newClaim);

        plugin.getPerkService().applyCurrentClaimPerk(player);

        if (!newClaim.equals(oldClaim) && !"NONE".equals(newClaim)) {
            ClaimProfile profile = plugin.getClaimProfileService().getOrCreate(context.get().claimId(), context.get().owner());
            if (profile.getSelectedPerk() != null) {
                String perk = plugin.getPerkService().getDefinition(profile.getSelectedPerk())
                        .map(PerkDefinition::displayName)
                        .orElse(profile.getSelectedPerk().name());
                player.sendActionBar(Chat.color(Chat.raw(plugin, "inside-claim").replace("%perk%", perk)));
            }
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getPerkService().handleFoodPerks(player) && event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && plugin.getPerkService().handleFallPerks(player)) {
            event.setDamage(event.getDamage() * 0.5D);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceStartSmeltEvent event) {
        Player nearest = event.getBlock().getWorld().getNearbyPlayers(event.getBlock().getLocation(), 8).stream().findFirst().orElse(null);
        if (nearest != null && plugin.getPerkService().handleSmeltPerk(nearest)) {
            event.setTotalCookTime(Math.max(20, (int) (event.getTotalCookTime() * 0.75)));
        }
    }
}
