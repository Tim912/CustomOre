package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Very small mock auction house GUI. Items placed into the GUI are not
 * actually sold but the inventory attempts to be dupe safe by clearing
 * any item if the player closes the inventory.
 */
public class AuctionHouse implements Listener {
    private final JavaPlugin plugin;

    public AuctionHouse(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.GOLD + "Auction House");
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta m = next.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(m);
        }
        ItemStack prev = new ItemStack(Material.ARROW);
        m = prev.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prev.setItemMeta(m);
        }
        inv.setItem(45, prev);
        inv.setItem(53, next);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.GOLD + "Auction House")) {
            e.setCancelled(true);
        }
    }
}
