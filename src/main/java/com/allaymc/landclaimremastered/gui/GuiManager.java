package com.allaymc.landclaimremastered.gui;

import com.allaymc.landclaimremastered.model.ClaimContext;
import com.allaymc.landclaimremastered.model.ClaimProfile;
import com.allaymc.landclaimremastered.model.PerkDefinition;
import com.allaymc.landclaimremastered.model.PerkKey;
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
        int totalBlocks = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 54, "§8AllayClaims");

        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Header
        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR, "§b§lAllayClaims", List.of(
                "§7Premium land progression for your claim.",
                "§8Claim ID: §f" + context.claimId()
        )));

        // Decorative corners
        inv.setItem(0, ItemUtil.make(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));
        inv.setItem(8, ItemUtil.make(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));
        inv.setItem(45, ItemUtil.make(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));
        inv.setItem(53, ItemUtil.make(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", List.of()));

        // Top info strip
        inv.setItem(19, ItemUtil.make(Material.EMERALD, "§a§lCurrent Tier", List.of(
                "§7Tier: §f" + tier.name(),
                "§7Total Claim Blocks: §f" + totalBlocks
        )));

        inv.setItem(21, ItemUtil.make(Material.BEACON, "§e§lActive Power", List.of(
                "§7Current perk on this claim:",
                "§f" + (profile.getSelectedPerk() == null ? "None Selected" : pretty(profile.getSelectedPerk().name())),
                "",
                "§8This is what you can show on scoreboard"
        )));

        inv.setItem(23, ItemUtil.make(Material.OAK_SIGN, "§6§lClaim Identity", List.of(
                "§7World: §f" + context.worldName(),
                "§7Area Blocks: §f" + context.areaBlocks(),
                "§7Claim ID: §f" + context.claimId()
        )));

        inv.setItem(25, ItemUtil.make(Material.COMPARATOR, "§d§lTrust Mode", List.of(
                "§7Mode: §f" + profile.getTrustMode().name(),
                "§7Controls who receives this claim perk."
        )));

        // Main navigation cards
        inv.setItem(37, ItemUtil.make(Material.NETHER_STAR, "§b§lClaim Perk Tree", List.of(
                "§7Browse the full progression track.",
                "§7Unlocked tiers are §aGreen§7.",
                "§7Locked tiers are §cRed§7.",
                "",
                "§eClick to open"
        )));

        inv.setItem(39, ItemUtil.make(Material.BEACON, "§a§lPerks", List.of(
                "§7See every perk.",
                "§7Unlocked and locked perks are both visible.",
                "§7Choose one active perk for this claim.",
                "",
                "§eClick to open"
        )));

        inv.setItem(41, ItemUtil.make(Material.PAPER, "§e§lClaim Status", List.of(
                "§7View your progression cleanly.",
                "§7Tier, blocks, next threshold, power.",
                "",
                "§eClick to open"
        )));

        inv.setItem(43, ItemUtil.make(Material.COMPARATOR, "§d§lMembers & Settings", List.of(
                "§7Trust mode and whitelist tools.",
                "§7Use command whitelist management too.",
                "",
                "§eClick to open"
        )));

        player.openInventory(inv);
    }

    public void openTreeMenu(Player player) {
        Tier currentTier = playerProgressService.currentTier(player);
        int totalBlocks = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 54, "§8Claim Perk Tree");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(4, ItemUtil.make(Material.NETHER_STAR, "§b§lClaim Perk Tree", List.of(
                "§7Total Claim Blocks: §f" + totalBlocks,
                "§7Current Tier: §f" + currentTier.name(),
                "",
                "§8A spaced progression layout"
        )));

        // spaced hypixel-like path
        int[] tierSlots = {10, 13, 16, 21, 24, 29, 32, 35, 40, 43};
        int[] connectorSlots = {11, 12, 14, 15, 22, 23, 30, 31, 33, 34, 41, 42};

        for (int slot : connectorSlots) {
            inv.setItem(slot, ItemUtil.make(Material.GRAY_STAINED_GLASS_PANE, " ", List.of()));
        }

        for (Tier tier : Tier.values()) {
            boolean unlocked = currentTier.getLevel() >= tier.getLevel();
            Material glass = unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;

            List<String> lore = new ArrayList<>();
            lore.add("§7Required Blocks: §f" + tierService.requiredBlocks(tier));
            lore.add("§7Status: " + (unlocked ? "§aUnlocked" : "§cLocked"));
            lore.add("");

            if (tier.getLevel() <= 5) {
                lore.add("§7Rewards:");
                perkService.allPerks().stream()
                        .filter(def -> def.unlockTier().getLevel() == tier.getLevel())
                        .sorted(Comparator.comparing(p -> p.key().name()))
                        .forEach(def -> lore.add("§8• §f" + def.displayName()));
            } else {
                lore.add("§cComing Soon");
            }

            inv.setItem(
                    tierSlots[tier.getLevel() - 1],
                    ItemUtil.make(glass, (unlocked ? "§a§lTier " : "§c§lTier ") + tier.name(), lore)
            );
        }

        inv.setItem(49, ItemUtil.make(Material.PAPER, "§b§lProgress Summary", List.of(
                "§7Current Tier: §f" + currentTier.name(),
                "§7Blocks: §f" + totalBlocks
        )));

        inv.setItem(53, ItemUtil.make(Material.ARROW, "§cBack", List.of("§7Return to main menu")));
        player.openInventory(inv);
    }

    public void openPerksMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());
        Tier currentTier = playerProgressService.currentTier(player);

        Inventory inv = Bukkit.createInventory(null, 54, "§8Claim Perks");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(4, ItemUtil.make(Material.BEACON, "§a§lClaim Perks", List.of(
                "§7Select one perk for this claim.",
                "§7Current Tier: §f" + currentTier.name(),
                "§7Selected: §f" + (profile.getSelectedPerk() == null ? "None" : pretty(profile.getSelectedPerk().name()))
        )));

        List<PerkDefinition> perks = perkService.allPerks().stream()
                .sorted(Comparator.comparingInt(p -> p.unlockTier().getLevel()))
                .toList();

        int[] slots = {
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28
        };

        for (int i = 0; i < perks.size() && i < slots.length; i++) {
            PerkDefinition perk = perks.get(i);

            boolean unlocked = currentTier.getLevel() >= perk.unlockTier().getLevel();
            boolean selected = profile.getSelectedPerk() == perk.key();

            Material icon = unlocked ? perk.icon() : Material.RED_STAINED_GLASS_PANE;

            List<String> lore = new ArrayList<>();
            lore.add("§7" + perk.description());
            lore.add("§7Unlock Tier: §f" + perk.unlockTier().name());
            lore.add("");

            if (selected) {
                lore.add("§bCurrently selected");
            } else if (unlocked) {
                lore.add("§eClick to select");
            } else {
                lore.add("§cLocked");
            }

            inv.setItem(
                    slots[i],
                    ItemUtil.make(icon, (selected ? "§b§l" : unlocked ? "§a" : "§c") + perk.displayName(), lore)
            );
        }

        inv.setItem(53, ItemUtil.make(Material.ARROW, "§cBack", List.of("§7Return to main menu")));
        player.openInventory(inv);
    }

    public void openStatusMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());
        Tier tier = playerProgressService.currentTier(player);
        int totalBlocks = playerProgressService.totalClaimBlocks(player);

        Inventory inv = Bukkit.createInventory(null, 45, "§8Claim Status");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(4, ItemUtil.make(Material.PAPER, "§e§lClaim Status", List.of(
                "§7A clean overview of your claim."
        )));

        inv.setItem(20, ItemUtil.make(Material.EMERALD, "§a§lProgress", List.of(
                "§7Current Tier: §f" + tier.name(),
                "§7Total Claim Blocks: §f" + totalBlocks,
                "§7Next Tier: §f" + nextThreshold(tier)
        )));

        inv.setItem(22, ItemUtil.make(Material.BEACON, "§b§lCurrent Power", List.of(
                "§7Selected Perk: §f" + (profile.getSelectedPerk() == null ? "None" : pretty(profile.getSelectedPerk().name())),
                "§7Trust Mode: §f" + profile.getTrustMode().name()
        )));

        inv.setItem(24, ItemUtil.make(Material.OAK_SIGN, "§6§lClaim Details", List.of(
                "§7Claim ID: §f" + context.claimId(),
                "§7World: §f" + context.worldName(),
                "§7Area Blocks: §f" + context.areaBlocks()
        )));

        inv.setItem(44, ItemUtil.make(Material.ARROW, "§cBack", List.of("§7Return to main menu")));
        player.openInventory(inv);
    }

    public void openSettingsMenu(Player player, ClaimContext context) {
        ClaimProfile profile = claimProfileService.getOrCreate(context.claimId(), context.owner());

        Inventory inv = Bukkit.createInventory(null, 45, "§8Claim Settings");
        fill(inv, Material.BLACK_STAINED_GLASS_PANE);

        inv.setItem(4, ItemUtil.make(Material.COMPARATOR, "§d§lMembers & Settings", List.of(
                "§7Manage who receives this claim's power."
        )));

        inv.setItem(20, ItemUtil.make(Material.COMPARATOR, "§d§lTrust Mode", List.of(
                "§7Current: §f" + profile.getTrustMode().name(),
                "",
                "§7OWNER_ONLY §8- owner only",
                "§7ALL_TRUSTED §8- trusted players",
                "§7WHITELIST_ONLY §8- command whitelist",
                "",
                "§eClick to cycle"
        )));

        inv.setItem(24, ItemUtil.make(Material.WRITABLE_BOOK, "§b§lWhitelist Commands", List.of(
                "§7/allayclaim whitelist list",
                "§7/allayclaim whitelist add <player>",
                "§7/allayclaim whitelist remove <player>"
        )));

        inv.setItem(44, ItemUtil.make(Material.ARROW, "§cBack", List.of("§7Return to main menu")));
        player.openInventory(inv);
    }

    private String nextThreshold(Tier tier) {
        if (tier == Tier.X) {
            return "MAX";
        }
        return String.valueOf(tierService.requiredBlocks(Tier.byLevel(tier.getLevel() + 1)));
    }

    private void fill(Inventory inv, Material material) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, ItemUtil.make(material, " ", List.of()));
        }
    }

    private String pretty(String raw) {
        String[] parts = raw.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            out.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1))
                    .append(' ');
        }
        return out.toString().trim();
    }
}
