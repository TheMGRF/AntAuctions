package net.antfarms.antauctions.guis;

import net.antfarms.antauctions.Utils;
import net.antfarms.anteconomy.Main;
import org.apache.commons.lang.StringUtils;
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

public class Pages {

    private static Plugin plugin = Main.getPlugin(Main.class);

    public static void open(Player player, int page) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

        int itemIndex = 45*(page - 1);

        Inventory inv = Bukkit.createInventory(null, 54, "§6§lAuction House §8#" + page);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try (Connection con = Main.sc.getSql().getConnection()) {

                    //Old Query: PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE " + System.currentTimeMillis() / 1000 + " <= item_time + 41400");
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_time > " + System.currentTimeMillis() / 1000);

                    ResultSet rs = ps.executeQuery();

                    // Setting item table
                    //PreparedStatement ps2 = con.prepareStatement("INSERT INTO ah_items(item_id, item_slot) VALUES ()");

                    //ps2.execute();

                    int slot = 0;
                    int loop = 0;
                    int selling = 0;
                    while (rs.next()) {
                        if (loop >= itemIndex && slot < 45) {
                            Material material = Material.valueOf(rs.getString("item_material").toUpperCase());
                            int amount = rs.getInt("item_amount");

                            ItemStack item = new ItemStack(material, amount);
                            item.setDurability(rs.getShort("item_durability"));
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

                            PreparedStatement nameGet = con.prepareStatement("SELECT username FROM players WHERE uuid = '" + rs.getString("player_uuid") + "' LIMIT 1");
                            ResultSet nameSet = nameGet.executeQuery();

                            String name = "";
                            while (nameSet.next()) {
                                name = nameSet.getString("username");
                            }

                            nameGet.close();
                            nameSet.close();

                            itemLore.add("§7Seller: §6" + name);
                            if (name.equals(player.getName())) {
                                selling++;
                            }

                            itemLore.add("§7Cost: §a$" + rs.getLong("item_price"));
                            itemLore.add("§7Time Remaining: §b" + Utils.setRemaining(rs.getLong("item_time") - Instant.now().getEpochSecond()));
                            itemLore.add("§7ID: §f" + Long.parseLong(StringUtils.stripStart(rs.getString("item_id"), "0")));

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
                        }
                        loop++;
                    }
                    ps.close();
                    con.close();

                    //Footer Navbar Content
                    Utils.setFooter(inv, selling);
                    //pass int of selling items
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        player.openInventory(inv);
    }

}
