package net.antfarms.antauctions.guis;

import net.antfarms.antauctions.Utils;
import net.antfarms.anteconomy.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CollectionBox {

    private static Plugin plugin = Main.getPlugin(Main.class);

    public static void open(Player player, int page) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        int itemIndex = 45*(page - 1);

        Inventory inv = Bukkit.createInventory(null, 54, "§6§lAuction House §8Collection #" + page);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = Main.sc.getSql().getConnection()) {

                    PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_bought = '" + player.getUniqueId().toString() + "'");
                    ResultSet rs = ps.executeQuery();

                    PreparedStatement ps2 = con.prepareStatement("SELECT * FROM auctionhouse WHERE player_uuid = '" + player.getUniqueId().toString() + "' AND item_time < " + System.currentTimeMillis() / 1000);
                    ResultSet rs2 = ps2.executeQuery();

                    int slot = 0;
                    int loop = 0;
                    while (rs.next()) {
                        if (loop >= itemIndex && slot < 45) {
                            while (rs2.next()) {
                                inv.setItem(slot, Utils.buildItem(rs2));
                                slot++;
                            }
                            inv.setItem(slot, Utils.buildItem(rs));
                            slot++;
                            loop++;
                        }
                    }
                    ps.close();
                    rs.close();
                    ps2.close();
                    rs2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        Utils.setPages(inv);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        backMeta.setDisplayName("§6§l< §eBack");

        back.setItemMeta(backMeta);

        inv.setItem(45, back);

        player.openInventory(inv);
    }

}
