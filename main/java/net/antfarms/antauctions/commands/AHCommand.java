package net.antfarms.antauctions.commands;

import com.meowj.langutils.lang.LanguageHelper;
import net.antfarms.antauctions.Main;
import net.antfarms.antauctions.guis.AuctionHouse;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

public class AHCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("ah")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (args.length != 0) {
                    if (args[0].equalsIgnoreCase("sell")) {
                        if (args.length > 1) {
                            try {
                                Long.parseLong(args[1]);
                            } catch (NumberFormatException e) {
                                player.sendMessage("§e● §cYou cannot sell an item for that much money.");
                                return true;
                            }

                            if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                ItemStack item = player.getInventory().getItemInMainHand();
                                ItemMeta meta = item.getItemMeta();
                                String itemName;
                                if (meta.hasDisplayName()) {
                                    itemName = meta.getDisplayName();
                                } else {
                                    if (item.getItemMeta().hasEnchants()) {
                                        itemName = "§b" + LanguageHelper.getItemName(item, "en_us");
                                    } else {
                                        itemName = "§f" + LanguageHelper.getItemName(item, "en_us");
                                    }

                                }
                                itemName = itemName.replaceAll("'", "");
                                int amount = item.getAmount();
                                String material = item.getType().name();
                                long durability = item.getDurability();

                                String enchanters = "";
                                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                                    enchanters = enchanters + entry.getKey().getName() + "#" + entry.getValue() + ",";
                                }
                                if (enchanters.equals("")) {
                                    enchanters = "none";
                                }

                                String lore = null;
                                if (item.getItemMeta().hasLore()) {
                                    lore = "";
                                    for (String loreLine : item.getItemMeta().getLore()) {
                                        lore = lore + loreLine + "%%";
                                    }
                                }

                                try (Connection con = Main.sc.getSql().getConnection()) {
                                    PreparedStatement ps = con.prepareStatement("INSERT INTO auctionhouse(player_uuid, item_name, item_lore, item_material, item_amount, item_enchants, item_durability, item_price, item_time) VALUES ('" + player.getUniqueId() + "', '" + itemName + "', '" + lore + "', '" + material + "', " + amount + ", '" + enchanters + "', " + durability + ", " + args[1] + ", " + (Instant.now().getEpochSecond() + 86400) + ")");

                                    ps.execute();

                                    ps.close();
                                    con.close();

                                    player.getInventory().getItemInMainHand().setAmount(0);
                                    player.sendMessage("§e● §aYou are now selling your " + itemName + " §aon the Auction House for §e$" + args[1]);
                                } catch (SQLException error) {
                                    error.printStackTrace();
                                    player.sendMessage("§e● §cAn error occured, we failed to list your item on the Auction House. Please notify an Admin of this ASAP.");
                                }
                            } else {
                                player.sendMessage("§e● §cPlease hold the item you wish to sell.");
                            }
                        } else {
                            player.sendMessage("§e● §cPlease specify how much you want to sell your item for.");
                        }
                    } else {
                        player.sendMessage("§e● §cSomething went wrong.");
                    }
                } else {
                    AuctionHouse.open(player);
                }

            } else {
                sender.sendMessage("u wot");
            }
            return true;
        }
        return false;
    }
}
