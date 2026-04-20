package com.allaymc.landclaimremastered.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemUtil {
    private ItemUtil() {
    }

    public static ItemStack make(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(Chat.color(name));
        meta.setLore(lore.stream().map(Chat::color).toList());
        item.setItemMeta(meta);
        return item;
    }
}
