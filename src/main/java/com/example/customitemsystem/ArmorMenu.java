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
 * Simple GUI to grant players themed armor sets.
 */
public class ArmorMenu implements Listener {
    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;

    public ArmorMenu(JavaPlugin plugin, AbilityManager abilityManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.BLUE + "Armor Sets");
        int slot = 0;
        for (Ability ability : Ability.values()) {
            if (slot >= 25) break;
            ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + ability.getDisplayName());
                item.setItemMeta(meta);
            }
            abilityManager.addAbility(item, ability);
            inv.setItem(slot++, item);
        }
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta meta = next.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(meta);
        }
        inv.setItem(26, next);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.BLUE + "Armor Sets")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (item.getType() == Material.ARROW) return;
            Player p = (Player) e.getWhoClicked();
            p.getInventory().addItem(item);
        }
    }
}
