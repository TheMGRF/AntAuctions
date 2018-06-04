package net.antfarms.antauctions;

import net.antfarms.antauctions.commands.AHCommand;
import net.antfarms.antauctions.events.InvClickEvent;
import net.antfarms.antauctions.events.JoinEvent;
import net.antfarms.anteconomy.AntEconomy;
import net.antfarms.serviceconnector.ServiceConnector;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main extends JavaPlugin {

    public static ServiceConnector sc;
    public static AntEconomy eco;

    public void onEnable() {
        //Depends
        sc = Bukkit.getServicesManager().getRegistration(ServiceConnector.class).getProvider();
        eco = Bukkit.getServicesManager().getRegistration(AntEconomy.class).getProvider();

        getCommand("ah").setExecutor(new AHCommand());
        //Events
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new InvClickEvent(), this);
    }


    private void createDB() {
        try (Connection con = Main.sc.getSql().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT 1 FROM auctionhouse LIMIT 1");

        } catch (SQLException e) {

        }
    }
}
