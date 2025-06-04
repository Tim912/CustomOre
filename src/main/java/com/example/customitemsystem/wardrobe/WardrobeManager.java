package com.example.customitemsystem.wardrobe;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Basic wardrobe system allowing players to store and equip armor sets.
 */
public class WardrobeManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, List<SetSlot>> sets = new HashMap<>();
    private final Map<UUID, Long> lastSwitch = new HashMap<>();
    private final NamespacedKey idKey;

    public WardrobeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "item_id");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private List<SetSlot> getSlots(Player player) {
        return sets.computeIfAbsent(player.getUniqueId(), k -> {
            List<SetSlot> list = new ArrayList<>();
            for (int i = 0; i < 18; i++) list.add(new SetSlot("Set " + (i + 1)));
            return list;
        });
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.BLUE + "Wardrobe");
        List<SetSlot> list = getSlots(player);
        for (int i = 0; i < 18; i++) {
            SetSlot slot = list.get(i);
            ItemStack display = new ItemStack(Material.LEATHER_CHESTPLATE);
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + slot.name);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Left click: equip");
                lore.add(ChatColor.YELLOW + "Shift + click: save current");
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(i, display);
        }
        player.openInventory(inv);
    }

    private void assignId(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(idKey, PersistentDataType.STRING)) {
            container.set(idKey, PersistentDataType.STRING, UUID.randomUUID().toString());
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!ChatColor.BLUE + "Wardrobe".equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 18) return;
        List<SetSlot> list = getSlots(player);
        SetSlot set = list.get(slot);
        if (e.isShiftClick()) {
            // save current armor
            ItemStack[] armor = player.getInventory().getArmorContents();
            set.armor = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                ItemStack piece = armor[i] == null ? null : armor[i].clone();
                assignId(piece);
                set.armor[i] = piece;
            }
            player.sendMessage(ChatColor.GREEN + "Saved armor to " + set.name);
            return;
        }
        long last = lastSwitch.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - last < 2000) {
            player.sendMessage(ChatColor.RED + "You must wait before switching sets again.");
            return;
        }
        if (set.armor == null) {
            player.sendMessage(ChatColor.RED + "This set is empty.");
            return;
        }
        player.getInventory().setArmorContents(Arrays.stream(set.armor).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new));
        lastSwitch.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "Equipped " + set.name);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!ChatColor.BLUE + "Wardrobe".equals(e.getView().getTitle())) return;
        // nothing to do
    }

    private static class SetSlot {
        final String name;
        ItemStack[] armor;

        SetSlot(String name) {
            this.name = name;
        }
    }
}
