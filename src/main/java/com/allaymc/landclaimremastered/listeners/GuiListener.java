package com.allaymc.landclaimremastered.listeners;

import com.allaymc.landclaimremastered.AllayClaimsPlugin;
import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.util.Chat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public final class GuiListener implements Listener {
    private final AllayClaimsPlugin plugin;

    public GuiListener(AllayClaimsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.equals("AllayClaims")
                && !title.equals("Claim Perk Tree")
                && !title.equals("Claim Perks")
                && !title.equals("Claim Status")
                && !title.equals("Claim Settings")) return;

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        Optional<ClaimContext> claimOptional = plugin.getPerkService().currentClaim(player);
        if (claimOptional.isEmpty()) {
            player.closeInventory();
            player.sendMessage(Chat.msg(plugin, "no-claim-here"));
            return;
        }

        ClaimContext context = claimOptional.get();
        ClaimProfile profile = plugin.getClaimProfileService().getOrCreate(context.claimId(), context.owner());

        switch (title) {
            case "AllayClaims" -> handleMain(player, context, item);
            case "Claim Perk Tree" -> plugin.getGuiManager().openMainMenu(player, context);
            case "Claim Status" -> plugin.getGuiManager().openMainMenu(player, context);
            case "Claim Settings" -> handleSettings(player, context, item);
            case "Claim Perks" -> handlePerks(player, context, item);
        }
    }

    private void handleMain(Player player, ClaimContext context, ItemStack item) {
        switch (item.getType()) {
            case NETHER_STAR -> plugin.getGuiManager().openTreeMenu(player);
            case BEACON -> plugin.getGuiManager().openPerksMenu(player, context);
            case OAK_SIGN -> plugin.getGuiManager().openStatusMenu(player, context);
            case COMPARATOR -> plugin.getGuiManager().openSettingsMenu(player, context);
            default -> {}
        }
    }

    private void handleSettings(Player player, ClaimContext context, ItemStack item) {
        if (item.getType() == Material.COMPARATOR) {
            if (!context.owner().equals(player.getUniqueId())) {
                player.sendMessage(Chat.msg(plugin, "owner-only"));
                return;
            }
            plugin.getClaimProfileService().toggleTrustMode(context.claimId(), context.owner());
            plugin.getGuiManager().openSettingsMenu(player, context);
        } else {
            plugin.getGuiManager().openMainMenu(player, context);
        }
    }

    private void handlePerks(Player player, ClaimContext context, ItemStack item) {
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            plugin.getGuiManager().openMainMenu(player, context);
            return;
        }
        if (!context.owner().equals(player.getUniqueId())) {
            player.sendMessage(Chat.msg(plugin, "owner-only"));
            return;
        }

        List<PerkDefinition> perks = plugin.getPerkService().allPerks().stream().toList();
        String stripped = item.getItemMeta() == null ? "" : org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
        for (PerkDefinition def : perks) {
            if (!stripped.equalsIgnoreCase(def.displayName())) continue;
            if (!plugin.getPerkService().isUnlocked(player, def.key())) {
                player.sendMessage(Chat.msg(plugin, "perk-locked"));
                return;
            }
            plugin.getClaimProfileService().setSelectedPerk(context.claimId(), context.owner(), def.key());
            player.sendMessage(Chat.raw(plugin, "prefix") + Chat.raw(plugin, "perk-set").replace("%perk%", def.displayName()));
            plugin.getGuiManager().openPerksMenu(player, context);
            return;
        }
    }
}
