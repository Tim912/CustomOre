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

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.BLUE + "Armor Sets");
        ArmorSet[] sets = ArmorSet.values();
        int start = page * 5;
        int end = Math.min(start + 5, sets.length);
        for (int i = start; i < end; i++) {
            ArmorSet set = sets[i];
            ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(set.getDisplayName());
                item.setItemMeta(meta);
            }
            abilityManager.addAbility(item, set.getAbility());
            inv.setItem(i - start, item);
        }
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta m = prev.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prev.setItemMeta(m);
        }
        ItemStack next = new ItemStack(Material.ARROW);
        m = next.getItemMeta();
        if (m != null) {
            m.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(m);
        }
        inv.setItem(45, prev);
        inv.setItem(53, next);
        player.openInventory(inv);
        player.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "page"), org.bukkit.persistence.PersistentDataType.INTEGER, page);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.BLUE + "Armor Sets")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            Player p = (Player) e.getWhoClicked();
            int page = p.getPersistentDataContainer().getOrDefault(new org.bukkit.NamespacedKey(plugin, "page"), org.bukkit.persistence.PersistentDataType.INTEGER, 0);
            if (item.getType() == Material.ARROW) {
                if (e.getSlot() == 45 && page > 0) {
                    open(p, page - 1);
                } else if (e.getSlot() == 53 && (page + 1) * 5 < ArmorSet.values().length) {
                    open(p, page + 1);
                }
                return;
            }
            p.getInventory().addItem(item);
        }
    }
}
