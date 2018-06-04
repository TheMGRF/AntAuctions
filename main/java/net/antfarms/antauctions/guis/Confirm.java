package net.antfarms.antauctions.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Confirm {

    public static void open(Player player, ItemStack item, Long id) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        Inventory inv = Bukkit.createInventory(null, 9, "§6§lPurchase §f#" + id);

        ItemStack confirm = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        ItemMeta confirmMeta = confirm.getItemMeta();

        confirmMeta.setDisplayName("§a§lCONFIRM");

        confirm.setItemMeta(confirmMeta);

        inv.setItem(0, confirm);
        inv.setItem(1, confirm);
        inv.setItem(2, confirm);
        inv.setItem(3, confirm);

        inv.setItem(4, item);

        ItemStack cancel = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        ItemMeta cancelMeta = cancel.getItemMeta();

        cancelMeta.setDisplayName("§c§lCANCEL");

        cancel.setItemMeta(cancelMeta);

        inv.setItem(5, cancel);
        inv.setItem(6, cancel);
        inv.setItem(7, cancel);
        inv.setItem(8, cancel);

        player.openInventory(inv);

    }

}
