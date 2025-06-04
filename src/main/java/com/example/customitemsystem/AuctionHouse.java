package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Very small auction house that allows players to list and buy items.
 */
public class AuctionHouse implements Listener {
    private final JavaPlugin plugin;
    private final List<ItemStack> listings = new ArrayList<>();

    public AuctionHouse(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.GOLD + "Auction House");
        int start = page * 45;
        int end = Math.min(start + 45, listings.size());
        for (int i = start; i < end; i++) {
            inv.addItem(listings.get(i));
        }
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta m = prev.getItemMeta();
        if (m != null) { m.setDisplayName(ChatColor.YELLOW + "Previous Page"); prev.setItemMeta(m); }
        ItemStack next = new ItemStack(Material.ARROW);
        m = next.getItemMeta();
        if (m != null) { m.setDisplayName(ChatColor.YELLOW + "Next Page"); next.setItemMeta(m); }
        inv.setItem(45, prev); inv.setItem(53, next);
        player.openInventory(inv);
        player.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "ah_page"), org.bukkit.persistence.PersistentDataType.INTEGER, page);
    }

    public void listItem(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        listings.add(item.clone());
        player.getInventory().removeItem(item);
        player.sendMessage(ChatColor.GREEN + "Item listed in the auction house.");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.GOLD + "Auction House")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            Player player = (Player) e.getWhoClicked();
            int page = player.getPersistentDataContainer().getOrDefault(new org.bukkit.NamespacedKey(plugin, "ah_page"), org.bukkit.persistence.PersistentDataType.INTEGER, 0);
            if (item.getType() == Material.ARROW) {
                if (e.getSlot() == 45 && page > 0) {
                    open(player, page - 1);
                } else if (e.getSlot() == 53 && (page + 1) * 45 < listings.size()) {
                    open(player, page + 1);
                }
                return;
            }
            listings.remove(item);
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "Purchased item from auction house.");
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(ChatColor.GOLD + "Auction House")) {
            // nothing to do - inventory view is read-only
        }
    }
}
