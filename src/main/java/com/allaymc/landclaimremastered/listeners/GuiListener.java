package your.package.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClaimGUIListener implements Listener {

    private final String GUI_TITLE = ChatColor.GOLD + "AllayClaim";

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Perks button
        ItemStack perks = new ItemStack(Material.EMERALD);
        ItemMeta perksMeta = perks.getItemMeta();
        perksMeta.setDisplayName(ChatColor.GREEN + "Claim Perks");
        perks.setItemMeta(perksMeta);
        inv.setItem(13, perks);

        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(closeMeta);
        inv.setItem(26, close);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryView view = event.getView();
        if (!view.getTitle().equals(GUI_TITLE)) return;

        // Cancel EVERYTHING by default
        event.setCancelled(true);

        // Only allow clicks in top inventory (GUI)
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(view.getTopInventory())) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();

        switch (item.getType()) {

            case EMERALD:
                player.sendMessage(ChatColor.GREEN + "Opening claim perks...");
                break;

            case BARRIER:
                player.closeInventory();
                break;

            default:
                break;
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);
        }
    }
}
