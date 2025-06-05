package com.example.customitemsystem;

import java.util.*;
import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
    private final Map<UUID, Integer> viewing = new HashMap<>();
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Simple auction house supporting buy-it-now listings with a listing fee.
 */
public class AuctionHouse implements Listener {
    private final JavaPlugin plugin;
    private final Map<Integer, Listing> listings = new HashMap<>();
    private int nextId = 1;
    private final NamespacedKey idKey;
    private final File saveFile;

    public AuctionHouse(JavaPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "item_id");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        File dir = new File(plugin.getDataFolder(), "save");
        if (!dir.exists()) dir.mkdirs();
        this.saveFile = new File(dir, "auctionhouse.yml");
        loadData();
    }

    /** Load listings from disk. */
    private void loadData() {
        if (!saveFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(saveFile);
        nextId = config.getInt("nextId", 1);
        ConfigurationSection sec = config.getConfigurationSection("listings");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                int id = Integer.parseInt(key);
                ItemStack item = sec.getItemStack(key + ".item");
                if (item == null) continue;
                Listing l = new Listing();
                l.id = id;
                l.item = item;
                l.itemId = sec.getString(key + ".itemId");
                l.seller = UUID.fromString(sec.getString(key + ".seller"));
                l.price = sec.getDouble(key + ".price");
                l.endTime = sec.getLong(key + ".endTime");
                listings.put(id, l);
            }
        }
    }

    /** Save listings to disk. */
    public void saveData() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("nextId", nextId);
        for (Listing l : listings.values()) {
            String key = "listings." + l.id;
            config.set(key + ".item", l.item);
            config.set(key + ".itemId", l.itemId);
            config.set(key + ".seller", l.seller.toString());
            config.set(key + ".price", l.price);
            config.set(key + ".endTime", l.endTime);
        }
        try {
            config.save(saveFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.GOLD + "Auction House");
        List<Listing> sorted = new ArrayList<>(listings.values());
        sorted.sort(Comparator.comparingInt(l -> l.id));
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < sorted.size(); i++) {
            Listing l = sorted.get(start + i);
            ItemStack display = l.item.clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore() == null ? new ArrayList<>() : new ArrayList<>(meta.getLore());
                lore.add("");
                long mins = Math.max(0, (l.endTime - System.currentTimeMillis()) / 60000);
                lore.add(ChatColor.YELLOW + "Price: " + l.price);
                lore.add(ChatColor.GRAY + "Time Left: " + mins + "m");
                lore.add(ChatColor.GRAY + "Seller: " + Bukkit.getOfflinePlayer(l.seller).getName());
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inv.setItem(i, display);
        }
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta m = prev.getItemMeta();
        if (m != null) { m.setDisplayName(ChatColor.YELLOW + "Previous Page"); prev.setItemMeta(m); }
        ItemStack next = new ItemStack(Material.ARROW);
        m = next.getItemMeta();
        if (m != null) { m.setDisplayName(ChatColor.YELLOW + "Next Page"); next.setItemMeta(m); }
        inv.setItem(45, prev);
        inv.setItem(53, next);
        player.openInventory(inv);
        player.getPersistentDataContainer().set(new NamespacedKey(plugin, "ah_page"), PersistentDataType.INTEGER, page);
    }

    public void listItem(Player player, ItemStack item, double price) {
        if (item == null || item.getType() == Material.AIR) return;
        assignId(item);
        String id = item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        for (Listing l : listings.values()) {
            if (l.itemId.equals(id)) {
                player.sendMessage(ChatColor.RED + "This item is already listed.");
                return;
            }
        }
        int fee = (int) Math.ceil(price * 0.05);
        if (player.getLevel() < fee) {
            player.sendMessage(ChatColor.RED + "You need " + fee + " experience levels to list this item.");
            return;
        }
        player.setLevel(player.getLevel() - fee);
        ItemStack clone = item.clone();
        player.getInventory().removeItem(item);
        Listing l = new Listing();
        l.id = nextId++;
        l.item = clone;
        l.itemId = id;
        l.seller = player.getUniqueId();
        l.price = price;
        l.endTime = System.currentTimeMillis() + 3600_000; // 1h
        listings.put(l.id, l);
        player.sendMessage(ChatColor.GREEN + "Item listed for " + price + " (fee " + fee + ")");
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(ChatColor.GOLD + "Auction House").equals(e.getView().getTitle())) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        int page = player.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "ah_page"), PersistentDataType.INTEGER, 0);
        if (item.getType() == Material.ARROW) {
            if (e.getSlot() == 45 && page > 0) {
                open(player, page - 1);
            } else if (e.getSlot() == 53 && (page + 1) * 45 < listings.size()) {
                open(player, page + 1);
            }
            return;
        }
        Listing target = null;
        for (Listing l : listings.values()) {
            if (l.item.isSimilar(item)) { target = l; break; }
        }
        if (target == null) return;
        openListing(player, target);
    }

    @EventHandler
    public void onListingClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.startsWith(ChatColor.GOLD + "Listing #")) return;
        e.setCancelled(true);
        Player player = (Player) e.getWhoClicked();
        Integer id = viewing.get(player.getUniqueId());
        if (id == null) return;
        Listing l = listings.get(id);
        if (l == null) { player.closeInventory(); return; }
        if (e.getSlot() == 7) {
            if (player.getUniqueId().equals(l.seller)) {
                int fee = (int) Math.ceil(l.price * 0.01);
                if (player.getLevel() < fee) {
                    player.sendMessage(ChatColor.RED + "Need " + fee + " levels to cancel.");
                    return;
                }
                player.setLevel(player.getLevel() - fee);
                player.getInventory().addItem(l.item);
                listings.remove(l.id);
                player.sendMessage(ChatColor.GREEN + "Listing cancelled.");
            } else {
                if (player.getLevel() < (int) l.price) {
                    player.sendMessage(ChatColor.RED + "Not enough experience levels.");
                    return;
                }
                player.setLevel(player.getLevel() - (int) l.price);
                player.getInventory().addItem(l.item);
                listings.remove(l.id);
                Player seller = Bukkit.getPlayer(l.seller);
                if (seller != null) seller.sendMessage(ChatColor.GREEN + player.getName() + " bought your item for " + l.price);
                player.sendMessage(ChatColor.GREEN + "Purchased item for " + l.price);
            }
            player.closeInventory();
            open(player, 0);
        } else if (e.getSlot() == 8) {
            player.closeInventory();
            open(player, 0);
        }
    }

    private void openListing(Player player, Listing l) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.GOLD + "Listing #" + l.id);
        ItemStack display = l.item.clone();
        inv.setItem(4, display);
        ItemStack action = new ItemStack(player.getUniqueId().equals(l.seller) ? Material.BARRIER : Material.EMERALD_BLOCK);
        ItemMeta meta = action.getItemMeta();
        if (meta != null) {
            if (player.getUniqueId().equals(l.seller)) {
                meta.setDisplayName(ChatColor.RED + "Cancel (1% fee)");
            } else {
                meta.setDisplayName(ChatColor.GREEN + "Buy for " + l.price);
            }
            action.setItemMeta(meta);
        }
        inv.setItem(7, action);
        ItemStack close = new ItemStack(Material.ARROW);
        meta = close.getItemMeta();
        if (meta != null) { meta.setDisplayName(ChatColor.YELLOW + "Back"); close.setItemMeta(meta); }
        inv.setItem(8, close);
        viewing.put(player.getUniqueId(), l.id);
        player.openInventory(inv);
    }

    private static class Listing {
        int id;
        ItemStack item;
        String itemId;
        UUID seller;
        double price;
        long endTime;
    }
}
