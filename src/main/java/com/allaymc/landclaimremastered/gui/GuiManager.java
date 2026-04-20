package com.allaymc.landclaimremastered.gui;

import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.model.Tier;
import com.allaymc.landclaimremastered.perks.PerkService;
import com.allaymc.landclaimremastered.service.ClaimProfileService;
import com.allaymc.landclaimremastered.service.PlayerProgressService;
import com.allaymc.landclaimremastered.service.TierService;
import com.allaymc.landclaimremastered.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GuiManager {
    private final TierService tierService;
    private final PerkService perkService;
    private final ClaimProfileService claimProfileService;
    private final PlayerProgressService playerProgressService;

    public GuiManager(
            TierService tierService,
            PerkService perkService,
            ClaimProfileService claimProfileService,
            PlayerProgressService playerProgressService
    ) {
        this.tierService = tierService;
        this.perkService = perkService;
        this.claimProfileService = claimProfileService;
        this.playerProgressService = playerProgressService;
    }

    public void openMainMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());
        Tier tier = playerProgressService.currentTier(player);
        int total = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 27, "AllayClaims");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(10, ItemUtil.make(Material.NETHER_STAR, "&bClaim Perk Tree", List.of(
                "&7Current Tier: &f" + tier.name(),
                "&7Total Claim Blocks: &f" + total,
                "&8Click to open"
        )));
        inv.setItem(12, ItemUtil.make(Material.BEACON, "&aPerks", List.of(
                "&7Selected Perk: &f" + (profile.getSelectedPerk() == null ? "None" : pretty(profile.getSelectedPerk().name())),
                "&8Click to manage"
        )));
        inv.setItem(14, ItemUtil.make(Material.OAK_SIGN, "&eClaim Status", List.of(
                "&7Claim ID: &f" + context.claimId(),
                "&7Area Blocks: &f" + context.areaBlocks(),
                "&8Click to open"
        )));
        inv.setItem(16, ItemUtil.make(Material.COMPARATOR, "&dMembers & Settings", List.of(
                "&7Trust Mode: &f" + profile.getTrustMode().name(),
                "&7Use /allayclaim whitelist add <player>",
                "&8Click to open"
        )));

        player.openInventory(inv);
    }

    public void openTreeMenu(Player player) {
        Tier currentTier = playerProgressService.currentTier(player);
        int totalBlocks = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 54, "Claim Perk Tree");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        int[] tierSlots = {10, 12, 14, 16, 20, 24, 28, 30, 32, 34};
        int[] spacerSlots = {11, 13, 15, 18, 22, 26, 29, 31, 33};
        for (int slot : spacerSlots) {
            inv.setItem(slot, ItemUtil.make(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }

        for (Tier tier : Tier.values()) {
            int slot = tierSlots[tier.getLevel() - 1];
            boolean unlocked = currentTier.getLevel() >= tier.getLevel();
            Material material = unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            List<String> lore = new ArrayList<>();
            lore.add("&7Required Blocks: &f" + tierService.requiredBlocks(tier));
            lore.add("&7Status: " + (unlocked ? "&aUnlocked" : "&cLocked"));

            if (tier.getLevel() <= 5) {
                lore.add("&7Rewards:");
                perkService.allPerks().stream()
                        .filter(perk -> perk.unlockTier() == tier)
                        .sorted(Comparator.comparing(p -> p.key().name()))
                        .forEach(perk -> lore.add("&8- &f" + perk.displayName()));
            } else {
                lore.add("&cComing Soon");
            }

            inv.setItem(slot, ItemUtil.make(material, (unlocked ? "&a" : "&c") + "Tier " + tier.name(), lore));
        }

        inv.setItem(49, ItemUtil.make(Material.PAPER, "&bProgress", List.of(
                "&7Total Claim Blocks: &f" + totalBlocks,
                "&7Current Tier: &f" + currentTier.name()
        )));
        player.openInventory(inv);
    }

    public void openPerksMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());
        Tier currentTier = playerProgressService.currentTier(player);

        Inventory inv = Bukkit.createInventory(null, 54, "Claim Perks");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        List<PerkDefinition> perks = perkService.allPerks().stream()
                .sorted(Comparator.comparingInt(p -> p.unlockTier().getLevel()))
                .toList();

        int slot = 10;
        for (PerkDefinition perk : perks) {
            boolean unlocked = currentTier.getLevel() >= perk.unlockTier().getLevel();
            boolean selected = perk.key() == profile.getSelectedPerk();
            Material icon = unlocked ? perk.icon() : Material.RED_STAINED_GLASS_PANE;

            List<String> lore = new ArrayList<>();
            lore.add("&7" + perk.description());
            lore.add("&7Unlock Tier: &f" + perk.unlockTier().name());
            if (selected) lore.add("&aSelected for this claim");
            else if (unlocked) lore.add("&eClick to select");
            else lore.add("&cLocked");

            inv.setItem(slot, ItemUtil.make(icon, (unlocked ? "&a" : "&c") + perk.displayName(), lore));
            slot++;
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot > 43) break;
        }

        player.openInventory(inv);
    }

    public void openStatusMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());
        Tier currentTier = playerProgressService.currentTier(player);
        int totalBlocks = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 27, "Claim Status");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(11, ItemUtil.make(Material.PAPER, "&bProgress", List.of(
                "&7Current Tier: &f" + currentTier.name(),
                "&7Total Claim Blocks: &f" + totalBlocks,
                "&7Next Tier At: &f" + nextThreshold(currentTier)
        )));
        inv.setItem(13, ItemUtil.make(Material.BEACON, "&aPerk Status", List.of(
                "&7Selected Perk: &f" + (profile.getSelectedPerk() == null ? "None" : pretty(profile.getSelectedPerk().name())),
                "&7Trust Mode: &f" + profile.getTrustMode().name()
        )));
        inv.setItem(15, ItemUtil.make(Material.OAK_SIGN, "&eClaim Info", List.of(
                "&7Claim ID: &f" + context.claimId(),
                "&7Area Blocks: &f" + context.areaBlocks(),
                "&7World: &f" + context.worldName()
        )));

        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());

        Inventory inv = Bukkit.createInventory(null, 27, "Claim Settings");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(11, ItemUtil.make(Material.COMPARATOR, "&dTrust Mode", List.of(
                "&7Current: &f" + profile.getTrustMode().name(),
                "&eClick to cycle mode"
        )));
        inv.setItem(15, ItemUtil.make(Material.WRITABLE_BOOK, "&bWhitelist Commands", List.of(
                "&7/allayclaim whitelist list",
                "&7/allayclaim whitelist add <player>",
                "&7/allayclaim whitelist remove <player>"
        )));

        player.openInventory(inv);
    }

    private String nextThreshold(Tier tier) {
        if (tier == Tier.X) return "MAX";
        return String.valueOf(tierService.requiredBlocks(Tier.byLevel(tier.getLevel() + 1)));
    }

    private void fill(Inventory inv, Material material) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, ItemUtil.make(material, " ", List.of()));
        }
    }

    private String pretty(String raw) {
        return raw.toLowerCase().replace('_', ' ');
    }
}
