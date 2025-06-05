package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.customitemsystem.stats.PlayerStats;
import com.example.customitemsystem.stats.StatsManager;

import java.util.*;

/**
 * Simple GUI based armor creator.
 */
public class CustomArmorCreator implements Listener {
    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;
    private final StatsManager statsManager;
    private final Map<UUID, Builder> builders = new HashMap<>();
    private final NamespacedKey pageKey;

    public CustomArmorCreator(JavaPlugin plugin, AbilityManager abilityManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        this.statsManager = statsManager;
        this.pageKey = new NamespacedKey(plugin, "ca_page");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Open the main creator menu for the player. */
    public void open(Player player) {
        Builder b = builders.computeIfAbsent(player.getUniqueId(), k -> new Builder());
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_AQUA + "Armor Creator");
        inv.setItem(10, item(Material.BOOK, ChatColor.YELLOW + "Ability", b.ability == null ? ChatColor.GRAY + "None" : b.ability.getDisplayName()));
        inv.setItem(12, item(Material.NETHER_STAR, ChatColor.YELLOW + "Rarity", b.rarity.getColor() + b.rarity.name()));
        inv.setItem(14, item(Material.DIAMOND, ChatColor.YELLOW + "Stats", ChatColor.GRAY + "Click to edit"));
        inv.setItem(16, item(Material.ANVIL, ChatColor.GREEN + "Create"));
        inv.setItem(26, item(Material.BARRIER, ChatColor.RED + "Cancel"));
        player.openInventory(inv);
    }

    private ItemStack item(Material m, String name, String... lore) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            it.setItemMeta(meta);
        }
        return it;
    }

    private void openAbility(Player player, int page) {
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.DARK_GREEN + "Select Ability");
        Ability[] arr = Ability.values();
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < arr.length; i++) {
            Ability a = arr[start + i];
            inv.setItem(i, item(Material.PAPER, a.getDisplayName()));
        }
        inv.setItem(45, item(Material.ARROW, ChatColor.YELLOW + "Prev"));
        inv.setItem(53, item(Material.ARROW, ChatColor.YELLOW + "Next"));
        player.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, page);
        player.openInventory(inv);
    }

    private void openRarity(Player player) {
        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.DARK_PURPLE + "Select Rarity");
        for (Rarity r : Rarity.values()) {
            inv.addItem(item(Material.DIAMOND, r.getColor() + r.name()));
        }
        player.openInventory(inv);
    }

    private void openStats(Player player) {
        Builder b = builders.get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.BLUE + "Edit Stats");
        inv.setItem(10, statItem("Strength", b.stats.strength));
        inv.setItem(11, statItem("Dexterity", b.stats.dexterity));
        inv.setItem(12, statItem("Defense", b.stats.defense));
        inv.setItem(13, statItem("Max Mana", b.stats.maxMana));
        inv.setItem(14, statItem("Mana Regen", b.stats.manaRegen));
        inv.setItem(15, statItem("Ability Damage", b.stats.abilityDamage));
        inv.setItem(16, statItem("Agility", b.stats.agility));
        inv.setItem(49, item(Material.ARROW, ChatColor.YELLOW + "Back"));
        player.openInventory(inv);
    }

    private ItemStack statItem(String name, int value) {
        return item(Material.PAPER, ChatColor.AQUA + name,
                ChatColor.GRAY + "Value: " + value,
                ChatColor.YELLOW + "Left +1, Right -1",
                ChatColor.YELLOW + "Shift = +/-5");
    }

    private void create(Player player) {
        Builder b = builders.get(player.getUniqueId());
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(b.rarity.format("Custom Armor"));
            item.setItemMeta(meta);
        }
        if (b.ability != null) abilityManager.addAbility(item, b.ability);
        statsManager.applyItemStats(item, b.stats);
        player.getInventory().addItem(item);
        builders.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Created custom armor.");
        player.closeInventory();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (title.equals(ChatColor.DARK_AQUA + "Armor Creator")) {
            e.setCancelled(true);
            switch (e.getRawSlot()) {
                case 10 -> openAbility(player, 0);
                case 12 -> openRarity(player);
                case 14 -> openStats(player);
                case 16 -> create(player);
                case 26 -> { builders.remove(player.getUniqueId()); player.closeInventory(); }
            }
        } else if (title.equals(ChatColor.DARK_GREEN + "Select Ability")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            int page = player.getPersistentDataContainer().getOrDefault(pageKey, PersistentDataType.INTEGER, 0);
            if (item.getType() == Material.ARROW) {
                if (e.getSlot() == 45 && page > 0) openAbility(player, page-1);
                else if (e.getSlot() == 53 && (page+1)*45 < Ability.values().length) openAbility(player, page+1);
                return;
            }
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            for (Ability a : Ability.values()) {
                if (a.getDisplayName().contains(name)) {
                    builders.get(player.getUniqueId()).ability = a;
                    break;
                }
            }
            open(player);
        } else if (title.equals(ChatColor.DARK_PURPLE + "Select Rarity")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            try {
                builders.get(player.getUniqueId()).rarity = Rarity.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            open(player);
        } else if (title.equals(ChatColor.BLUE + "Edit Stats")) {
            e.setCancelled(true);
            if (e.getSlot() == 49) { open(player); return; }
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            String stat = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            int delta = e.isShiftClick() ? 5 : 1;
            if (e.isRightClick()) delta = -delta;
            Builder b = builders.get(player.getUniqueId());
            switch (stat) {
                case "Strength" -> b.stats.strength = Math.max(0, b.stats.strength + delta);
                case "Dexterity" -> b.stats.dexterity = Math.max(0, b.stats.dexterity + delta);
                case "Defense" -> b.stats.defense = Math.max(0, b.stats.defense + delta);
                case "Max Mana" -> b.stats.maxMana = Math.max(0, b.stats.maxMana + delta);
                case "Mana Regen" -> b.stats.manaRegen = Math.max(0, b.stats.manaRegen + delta);
                case "Ability Damage" -> b.stats.abilityDamage = Math.max(0, b.stats.abilityDamage + delta);
                case "Agility" -> b.stats.agility = Math.max(0, b.stats.agility + delta);
            }
            openStats(player);
        }
    }

    private static class Builder {
        Ability ability;
        Rarity rarity = Rarity.COMMON;
        PlayerStats stats = new PlayerStats();
    }
}
