package net.antfarms.antauctions;

import net.antfarms.antauctions.guis.AuctionHouse;
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

public class Utils {

    private static Plugin plugin = Main.getPlugin(Main.class);

    public static void buyError(Inventory inv, int slot, ItemStack originalItem, String error) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName("§c" + error);

        item.setItemMeta(itemMeta);

        inv.setItem(slot, item);

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                inv.setItem(slot, originalItem);
            }
        }, 20L);
    }

    public static boolean itemExists(Long id) {
        try (Connection con = Main.sc.getSql().getConnection()) {

            PreparedStatement ps = con.prepareStatement("SELECT * FROM auctionhouse WHERE item_id = '" + id + "' LIMIT 1");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getLong("item_id") == id) {
                    ps.close();
                    rs.close();
                    return true;
                } else {
                    ps.close();
                    rs.close();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void notAvailable(Player player) {
        player.sendMessage("§e● §cSorry that item is no longer for sale.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
        AuctionHouse.open(player);
    }

    public static void setFooter(Inventory inv, int sellingAmount) {
        // SELLING ITEMS
        ItemStack selling = new ItemStack(Material.CHEST);
        ItemMeta sellingMeta = selling.getItemMeta();

        sellingMeta.setDisplayName("§6Items You Are Selling");
        ArrayList<String> sellingLore = new ArrayList<>();
        sellingLore.add("§7This section will allow you to view");
        sellingLore.add("§7all the items you are currently selling.");
        sellingLore.add("");
        sellingLore.add("§aCurrently Selling: §f" + sellingAmount);

        sellingMeta.setLore(sellingLore);
        selling.setItemMeta(sellingMeta);

        inv.setItem(45, selling);

        // EXPIRED ITEMS
        ItemStack expired = new ItemStack(Material.ENDER_CHEST);
        ItemMeta expiredMeta = expired.getItemMeta();

        expiredMeta.setDisplayName("§6Collection Box");
        ArrayList<String> expiredLore = new ArrayList<>();
        expiredLore.add("§7This section allows you to collect any");
        expiredLore.add("§7purchased or expired items.");
        expiredLore.add("");
        expiredLore.add("§aItems To Collect: §f0");

        expiredMeta.setLore(expiredLore);
        expired.setItemMeta(expiredMeta);

        inv.setItem(46, expired);

        setPages(inv);

        // HOW
        ItemStack how = new ItemStack(Material.EMERALD);
        ItemMeta howMeta = how.getItemMeta();

        howMeta.setDisplayName("§6How to sell?");

        ArrayList<String> howLore = new ArrayList<>();
        howLore.add("§7To list an item on the Auction House simply");
        howLore.add("§7hold it and type §a/ah sell <price> §7in chat.");

        howMeta.setLore(howLore);
        how.setItemMeta(howMeta);

        inv.setItem(52, how);

        // WHAT
        ItemStack what = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta whatMeta = what.getItemMeta();

        whatMeta.setDisplayName("§6What is this?");

        ArrayList<String> whatLore = new ArrayList<>();
        whatLore.add("§7This is a player driven §6§lAuction House§7.");
        whatLore.add("§7Every player can put items up for §csale §7and");
        whatLore.add("§7§cbuy §7items that other players have listed.");
        whatLore.add("");
        whatLore.add("§7Every item has a time limit of §b24 hours§7.");
        whatLore.add("§7If an item is not §apurchased §7after §b24 hours");
        whatLore.add("§7the item will be returned to the seller where");
        whatLore.add("§7they can collect it from the §6Collection Box§7.");

        whatMeta.setLore(whatLore);
        what.setItemMeta(whatMeta);

        inv.setItem(53, what);
    }

    public static void setPages(Inventory inv) {
        // LAST PAGE
        ItemStack lastPage = new ItemStack(Material.EMPTY_MAP);
        ItemMeta lastPageMeta = lastPage.getItemMeta();

        lastPageMeta.setDisplayName("§6§l< §eLast Page");

        lastPage.setItemMeta(lastPageMeta);

        inv.setItem(48, lastPage);

        // REFRESH
        ItemStack refreshPage = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta refreshPageMeta = refreshPage.getItemMeta();

        refreshPageMeta.setDisplayName("§6§lRefresh");

        refreshPage.setItemMeta(refreshPageMeta);

        inv.setItem(49, refreshPage);

        // NEXT PAGE
        ItemStack nextPage = new ItemStack(Material.MAP);
        ItemMeta nextPageMeta = nextPage.getItemMeta();

        nextPageMeta.setDisplayName("§eNext Page §6§l>");

        nextPage.setItemMeta(nextPageMeta);

        inv.setItem(50, nextPage);
    }

    public static String setRemaining(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeString = String.format("%02d Hours, %02d Minutes, %02d Seconds", hours, minutes, seconds);
        return timeString;
    }

    public static int getPage(Inventory inv) {
        String blah = inv.getName().replace("#", "@");
        String[] title = blah.split("@");
        return Integer.parseInt(title[1]);
    }

    public static ItemStack buildItem(ResultSet rs) {
        try {
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

            int id = rs.getInt("item_id");
            itemLore.add("§7Click to §aCollect");
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

            return item;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ItemStack(Material.STONE);
    }

}
