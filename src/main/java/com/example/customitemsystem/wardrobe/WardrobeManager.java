package com.example.customitemsystem.wardrobe;

import java.util.*;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

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
    private final NamespacedKey lockKey;
    private final File dataDir;

    public WardrobeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "item_id");
        this.lockKey = new NamespacedKey(plugin, "wardrobe_lock");
        this.dataDir = new File(plugin.getDataFolder(), "wardrobe");
        if (!dataDir.exists()) dataDir.mkdirs();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private List<SetSlot> getSlots(Player player) {
        return sets.computeIfAbsent(player.getUniqueId(), k -> loadSlots(player.getUniqueId()));
    }

    private List<SetSlot> loadSlots(UUID id) {
        File f = new File(dataDir, id.toString() + ".yml");
        List<SetSlot> list = new ArrayList<>();
        if (f.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            for (int i = 0; i < 18; i++) {
                String path = "slots." + i;
                String name = cfg.getString(path + ".name", "Set " + (i + 1));
                SetSlot slot = new SetSlot(name);
                List<?> raw = cfg.getList(path + ".armor");
                if (raw != null) {
                    List<ItemStack> items = (List<ItemStack>) (List<?>) raw;
                    slot.armor = items.toArray(new ItemStack[0]);
                }
                list.add(slot);
            }
        } else {
            for (int i = 0; i < 18; i++) list.add(new SetSlot("Set " + (i + 1)));
        }
        return list;
    }

    private void saveSlots(UUID id) {
        List<SetSlot> list = sets.get(id);
        if (list == null) return;
        File f = new File(dataDir, id.toString() + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        for (int i = 0; i < list.size(); i++) {
            SetSlot slot = list.get(i);
            String path = "slots." + i;
            cfg.set(path + ".name", slot.name);
            if (slot.armor != null)
                cfg.set(path + ".armor", Arrays.asList(slot.armor));
        }
        try { cfg.save(f); } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveAll() {
        for (UUID id : sets.keySet()) saveSlots(id);
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
                lore.add(ChatColor.YELLOW + "Shift-left: save current");
                lore.add(ChatColor.YELLOW + "Shift-right: withdraw set");
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(i, display);
        }
        ItemStack none = new ItemStack(Material.BARRIER);
        ItemMeta nm = none.getItemMeta();
        if (nm != null) { nm.setDisplayName(ChatColor.RED + "Unequip"); none.setItemMeta(nm); }
        inv.setItem(22, none);
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

    private void addLock(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(lockKey, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
    }

    private void removeLock(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().remove(lockKey);
        item.setItemMeta(meta);
    }

    private boolean isLocked(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(lockKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!ChatColor.BLUE + "Wardrobe".equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        int slot = e.getRawSlot();
        if (slot == 22) {
            // unequip
            for (ItemStack item : player.getInventory().getArmorContents()) {
                removeLock(item);
            }
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.sendMessage(ChatColor.GREEN + "Armor removed");
            return;
        }
        if (slot < 0 || slot >= 18) return;
        List<SetSlot> list = getSlots(player);
        SetSlot set = list.get(slot);
        if (e.isShiftClick()) {
            if (e.isLeftClick()) {
                // save current armor and remove it
                ItemStack[] armor = player.getInventory().getArmorContents();
                set.armor = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    ItemStack piece = armor[i] == null ? null : armor[i].clone();
                    assignId(piece);
                    set.armor[i] = piece;
                }
                player.getInventory().setArmorContents(new ItemStack[4]);
                player.sendMessage(ChatColor.GREEN + "Saved armor to " + set.name);
                return;
            } else if (e.isRightClick()) {
                // withdraw stored armor
                if (set.armor != null) {
                    for (ItemStack piece : set.armor) {
                        if (piece != null)
                            player.getInventory().addItem(piece);
                    }
                    set.armor = null;
                    player.sendMessage(ChatColor.YELLOW + "Removed armor from " + set.name);
                    return;
                }
            }
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
        ItemStack[] clone = Arrays.stream(set.armor).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
        for (ItemStack piece : clone) addLock(piece);
        player.getInventory().setArmorContents(clone);
        lastSwitch.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ChatColor.GREEN + "Equipped " + set.name);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!ChatColor.BLUE + "Wardrobe".equals(e.getView().getTitle())) return;
    }

    @EventHandler
    public void onMove(InventoryClickEvent e) {
        if (ChatColor.BLUE + "Wardrobe".equals(e.getView().getTitle())) return;
        if (isLocked(e.getCurrentItem()) || isLocked(e.getCursor())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent e) {
        if (isLocked(e.getItemDrop().getItemStack())) e.setCancelled(true);
    }

    private static class SetSlot {
        final String name;
        ItemStack[] armor;

        SetSlot(String name) {
            this.name = name;
        }
    }
}
