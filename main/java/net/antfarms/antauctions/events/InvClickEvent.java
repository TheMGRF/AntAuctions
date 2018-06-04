package net.antfarms.antauctions.events;

import net.antfarms.antauctions.Main;
import net.antfarms.antauctions.Utils;
import net.antfarms.antauctions.guis.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InvClickEvent implements Listener {

    private Plugin plugin = Main.getPlugin(Main.class);

    @SuppressWarnings("Duplicates")
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getName().contains("§6§lAuction House §8#")) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null) {
                if (e.getClickedInventory().getName().contains("§6§lAuction House §8#")) {
                    if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        if (e.getSlot() < 45) {

                            int page = Utils.getPage(e.getInventory());

                            int itemIndex = 45*(page - 1);

                            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    Long itemID = null;
                                    if (e.getCurrentItem().getItemMeta().hasLore()) {
                                        for (String idLine : e.getCurrentItem().getItemMeta().getLore()) {
                                            if (idLine.startsWith("§7Seller: ")) {
                                                String[] parts = idLine.split(" ");
                                                String name = parts[1].replace("§6", "");
                                                if (name.equals(player.getName())) {
                                                    Utils.buyError(e.getClickedInventory(), e.getSlot(), e.getCurrentItem(), "You cannot buy your own items.");
                                                    return;
                                                }
                                            }

                                            if (idLine.startsWith("§7ID: ")) {
                                                String[] parts = idLine.split(" ");
                                                String num = parts[1].replace("§f", "");
                                                itemID = Long.parseLong(StringUtils.stripStart(num, "0"));
                                            }
                                        }
                                    } else {
                                        return;
                                    }

                                    try (Connection con = Main.sc.getSql().getConnection()) {

                                        PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse");

                                        ResultSet rs = ps.executeQuery();

                                        //int loop = 0;
                                        //int slot = 0;
                                        while (rs.next()) {
                                            Long id = rs.getLong("item_id");
                                            if (Long.parseLong(StringUtils.stripStart(id.toString(), "0")) == itemID) {
                                                if (Utils.itemExists(id)) {
                                                    if (Main.eco.getBalance(player.getName()) >= rs.getLong("item_price")) {
                                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Confirm.open(player, e.getCurrentItem(), id);
                                                            }
                                                        });
                                                        return;
                                                    } else {
                                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Utils.buyError(e.getClickedInventory(), e.getSlot(), e.getCurrentItem(), "You cannot afford this.");
                                                            }
                                                        });
                                                        return;
                                                    }
                                                } else {
                                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            player.sendMessage("§e● §cSorry that item is no longer for sale.");
                                                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                                                            AuctionHouse.open(player);
                                                        }
                                                    });
                                                    return;
                                                }
                                            }
                                        }
                                        ps.close();
                                        con.close();
                                        return;
                                    } catch (SQLException error) {
                                        error.printStackTrace();
                                        return;
                                    }
                                }
                            });
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Items You Are Selling")) {
                            Selling.open(player);
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6Collection Box")) {
                            CollectionBox.open(player, 1);
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6§lRefresh")) {
                            AuctionHouse.open(player);
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eNext Page §6§l>")) {
                            if (e.getInventory().getItem(44) != null) {
                                int page = Utils.getPage(e.getInventory());

                                Pages.open(player, page + 1);
                            } else {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                            }
                        } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6§l< §eLast Page")) {
                            int page = Utils.getPage(e.getInventory());
                            if (page > 1) {
                                Pages.open(player, page - 1);
                            } else {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                            }
                        }
                    }
                }
            }

        } else if (e.getInventory().getName().contains("§6§lPurchase")) {
            e.setCancelled(true);
            if (e.getClickedInventory() != null) {
                if (e.getClickedInventory().getName().contains("§6§lPurchase")) {
                    if (e.getCurrentItem() != null) {
                        ItemStack item = e.getCurrentItem();
                        if (item.hasItemMeta()) {
                            if (item.getItemMeta().hasDisplayName()) {
                                if (item.getItemMeta().getDisplayName().equals("§a§lCONFIRM")) {

                                    String blah = e.getInventory().getName().replace("#", "@");
                                    String[] title = blah.split("@");
                                    long itemID = Integer.parseInt(title[1]);

                                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            try (Connection con = Main.sc.getSql().getConnection()) {

                                                PreparedStatement set = con.prepareStatement("UPDATE auctionhouse SET item_bought = '" + player.getUniqueId().toString() + "' WHERE item_id = " + itemID);

                                                set.execute();
                                                set.close();

                                                PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_id = " + itemID + " LIMIT 1");

                                                ResultSet rs = ps.executeQuery();

                                                while (rs.next()) {
                                                    if (Utils.itemExists(rs.getLong("item_id"))) {

                                                        final long cost = rs.getLong("item_price");

                                                        String name = rs.getString("item_name");

                                                        /*ItemStack purchase = new ItemStack(Material.valueOf(rs.getString("item_material")), rs.getInt("item_amount"), rs.getByte("item_durability"));
                                                        ItemMeta purchaseMeta = purchase.getItemMeta();


                                                        purchaseMeta.setDisplayName(name);

                                                        ArrayList<String> purchaseLore = new ArrayList<>();
                                                        if (!rs.getString("item_lore").equals("null")) {
                                                            String[] lore = rs.getString("item_lore").split("%%");
                                                            for (String loreLine : lore) {
                                                                purchaseLore.add(loreLine);
                                                            }
                                                        }
                                                        purchaseMeta.setLore(purchaseLore);

                                                        if (!rs.getString("item_enchants").equals("none")) {
                                                            String[] enchants = rs.getString("item_enchants").split(",");
                                                            for (String enchanta : enchants) {
                                                                String[] enchantb = enchanta.split("#");

                                                                purchaseMeta.addEnchant(Enchantment.getByName(enchantb[0]), Integer.parseInt(enchantb[1]), true);
                                                            }
                                                        }

                                                        purchase.setItemMeta(purchaseMeta);*/

                                                        int amount = rs.getInt("item_amount");

                                                        PreparedStatement nameGet = con.prepareStatement("SELECT username FROM players WHERE uuid = '" + rs.getString("player_uuid") + "' LIMIT 1");
                                                        ResultSet nameSet = nameGet.executeQuery();

                                                        String seller = "";
                                                        while (nameSet.next()) {
                                                            seller = nameSet.getString("username");
                                                        }

                                                        nameGet.close();
                                                        nameSet.close();

                                                        Main.eco.removeMoney(player.getName(), cost);
                                                        Main.eco.addMoney(seller, cost);

                                                        final String sellerName = seller;

                                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                //player.getInventory().addItem(purchase);
                                                                player.sendMessage("§e● §aSuccessfully bought §e" + amount + "x §e" + name + " §afor §e$" + cost);
                                                                player.sendMessage("   §7This item is now available in your §6Collection Box§7.");
                                                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);
                                                                player.closeInventory();
                                                                if (Bukkit.getPlayer(sellerName) != null) {
                                                                    Player sellerPlayer = Bukkit.getPlayer(sellerName);
                                                                    sellerPlayer.sendMessage("§e● §a" + player.getName() + "just bought your §e" + amount + "x §e" + name + " §afor §e$" + cost);
                                                                    sellerPlayer.playSound(sellerPlayer.getLocation(), Sound.BLOCK_NOTE_CHIME, 1, 1);
                                                                }
                                                            }
                                                        });

                                                        // Delete Item from DB
                                                        //PreparedStatement ps2 = con.prepareStatement("DELETE FROM auctionhouse WHERE item_id = " + itemID + " LIMIT 1");
                                                        //ps2.execute();
                                                        //ps2.close();
                                                    } else {
                                                        Utils.notAvailable(player);
                                                    }
                                                }
                                            } catch (SQLException error) {
                                                error.printStackTrace();
                                            }
                                        }
                                    });
                                    return;
                                } else if (item.getItemMeta().getDisplayName().equals("§c§lCANCEL")) {
                                    AuctionHouse.open(player);
                                    return;
                                }
                                Utils.notAvailable(player);
                            }
                        }
                    }
                }
            }
        } else if (e.getInventory().getName().contains("§6§lAuction House §8Selling")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                ItemStack item = e.getCurrentItem();

                if (item.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().getDisplayName().equals("§6§l< §eBack")) {
                        Pages.open(player, 1);
                    } else {
                        Long id = null;
                        for (String idLine : item.getItemMeta().getLore()) {
                            if (idLine.startsWith("§7ID: ")) {
                                String[] parts = idLine.split(" ");
                                String num = parts[1].replace("§f", "");
                                id = Long.parseLong(StringUtils.stripStart(num, "0"));
                            }
                        }
                        final Long itemID = id;

                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                            @Override
                            public void run() {
                                try (Connection con = Main.sc.getSql().getConnection()) {

                                    PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_id = " + itemID + " LIMIT 1");

                                    ResultSet rs = ps.executeQuery();

                                    while (rs.next()) {
                                        ItemStack item = new ItemStack(Material.valueOf(rs.getString("item_material")), rs.getInt("item_amount"), rs.getByte("item_durability"));
                                        ItemMeta itemMeta = item.getItemMeta();

                                        itemMeta.setDisplayName(rs.getString("item_name"));

                                        ArrayList<String> purchaseLore = new ArrayList<>();
                                        if (!rs.getString("item_lore").equals("null")) {
                                            String[] lore = rs.getString("item_lore").split("%%");
                                            for (String loreLine : lore) {
                                                purchaseLore.add(loreLine);
                                            }
                                        }
                                        itemMeta.setLore(purchaseLore);

                                        if (!rs.getString("item_enchants").equals("none")) {
                                            String[] enchants = rs.getString("item_enchants").split(",");
                                            for (String enchanta : enchants) {
                                                String[] enchantb = enchanta.split("#");

                                                itemMeta.addEnchant(Enchantment.getByName(enchantb[0]), Integer.parseInt(enchantb[1]), true);
                                            }
                                        }

                                        item.setItemMeta(itemMeta);

                                        player.getInventory().addItem(item);

                                        PreparedStatement ps2 = con.prepareStatement("DELETE FROM auctionhouse WHERE item_id = " + itemID);
                                        ps2.execute();

                                        ps2.close();
                                    }
                                    ps.close();
                                    rs.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        Selling.open(player);
                    }
                }
            }
        } else if (e.getInventory().getName().contains("§6§lAuction House §8Collection")) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                ItemStack item = e.getCurrentItem();

                if (e.getSlot() < 45) {

                }

                if (item.getItemMeta().hasDisplayName()) {
                    if (item.getItemMeta().getDisplayName().equals("§6§l< §eBack")) {
                        Pages.open(player, 1);
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6§lRefresh")) {
                        CollectionBox.open(player, 1);
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§eNext Page §6§l>")) {
                        if (e.getInventory().getItem(44) != null) {
                            int page = Utils.getPage(e.getInventory());

                            CollectionBox.open(player, page + 1);
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        }
                    } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§6§l< §eLast Page")) {
                        int page = Utils.getPage(e.getInventory());
                        if (page > 1) {
                            CollectionBox.open(player, page - 1);
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
                        }
                    }
                }
            }
        }
    }

}
