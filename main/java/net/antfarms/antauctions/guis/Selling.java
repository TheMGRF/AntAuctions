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
import java.time.Instant;
import java.util.ArrayList;

public class Selling {

    private static Plugin plugin = Main.getPlugin(Main.class);

    public static void open(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        Inventory inv = Bukkit.createInventory(null, 54, "§6§lAuction House §8Selling");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = Main.sc.getSql().getConnection()) {

                    PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_time > " + System.currentTimeMillis() / 1000 + " AND player_uuid = '" + player.getUniqueId().toString() + "'");

                    ResultSet rs = ps.executeQuery();

                    int slot = 0;
                    while (rs.next()) {
                        if (slot < 45) {
                            ItemStack item = new ItemStack(Material.valueOf(rs.getString("item_material")), rs.getInt("item_amount"), rs.getByte("item_durability"));
                            ItemMeta itemMeta = item.getItemMeta();

                            itemMeta.setDisplayName(rs.getString("item_name"));

                            ArrayList<String> itemLore = new ArrayList<>();
                            if (!rs.getString("item_lore").equals("null")) {
                                String[] lore = rs.getString("item_lore").split("%%");
                                for (String loreLine : lore) {
                                    itemLore.add(loreLine);
                                }
                            }
                            itemLore.add("");
                            itemLore.add("§7Price: §a$" + rs.getLong("item_price"));
                            itemLore.add("§7Time Remaining: §b" + Utils.setRemaining(rs.getLong("item_time") - Instant.now().getEpochSecond()));
                            itemLore.add("");

                            int id = rs.getInt("item_id");
                            itemLore.add("§7Click to §cCancel");
                            itemLore.add("");
                            itemLore.add("§7ID: §f" + id);

                            itemMeta.setLore(itemLore);

                            if (!rs.getString("item_enchants").equals("none")) {
                                String[] enchants = rs.getString("item_enchants").split(",");
                                for (String enchanta : enchants) {
                                    String[] enchantb = enchanta.split("#");

                                    itemMeta.addEnchant(Enchantment.getByName(enchantb[0]), Integer.parseInt(enchantb[1]), true);
                                }
                            }

                            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                            item.setItemMeta(itemMeta);

                            inv.setItem(slot, item);
                            slot++;
                        } else {
                            return;
                        }
                    }
                    ps.close();
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        backMeta.setDisplayName("§6§l< §eBack");

        back.setItemMeta(backMeta);

        inv.setItem(45, back);

        player.openInventory(inv);
    }

}
