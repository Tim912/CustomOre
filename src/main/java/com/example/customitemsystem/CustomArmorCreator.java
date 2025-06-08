package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.customitemsystem.stats.PlayerStats;
import com.example.customitemsystem.stats.StatsManager;
import com.example.customitemsystem.ArmorPiece;

import java.util.*;
import java.util.function.Consumer;

/**
 * GUI based item creator with set bonuses and item list.
 */
public class CustomArmorCreator implements Listener {
    private final JavaPlugin plugin;
    private final AbilityManager abilityManager;
    private final StatsManager statsManager;
    private final Map<UUID, Builder> builders = new HashMap<>();
    private final NamespacedKey pageKey;
    private final List<ItemStack> createdItems = new ArrayList<>();
    private final Map<UUID, Consumer<String>> chatWait = new HashMap<>();

    public CustomArmorCreator(JavaPlugin plugin, AbilityManager abilityManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.abilityManager = abilityManager;
        this.statsManager = statsManager;
        this.pageKey = new NamespacedKey(plugin, "ca_page");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** Open the main menu for the player. */
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_AQUA + "Custom Items");
        inv.setItem(11, item(Material.ANVIL, ChatColor.GREEN + "Create Item"));
        inv.setItem(15, item(Material.CHEST, ChatColor.YELLOW + "View Created"));
        player.openInventory(inv);
    }

    /** Open the item creation menu. */
    private void openCreator(Player player) {
        Builder b = builders.computeIfAbsent(player.getUniqueId(), k -> new Builder());
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_AQUA + "Item Creator");
        inv.setItem(9, item(b.base == null ? Material.CHEST : b.base.getType(), ChatColor.YELLOW + "Base Item", b.base == null ? ChatColor.GRAY + "Click to select" : b.base.getType().name()));
        inv.setItem(10, item(Material.BOOK, ChatColor.YELLOW + "Ability", b.ability == null ? ChatColor.GRAY + "None" : b.ability.getDisplayName()));
        inv.setItem(11, item(Material.NAME_TAG, ChatColor.YELLOW + "Item Name", b.name == null ? ChatColor.GRAY + "Click to set" : b.name));
        inv.setItem(12, item(Material.NETHER_STAR, ChatColor.YELLOW + "Rarity", b.rarity.getColor() + b.rarity.name()));
        inv.setItem(13, item(Material.LEATHER_CHESTPLATE, ChatColor.YELLOW + "Pieces", ChatColor.GRAY + b.pieces.size() + " selected"));
        inv.setItem(14, item(Material.DIAMOND, ChatColor.YELLOW + "Stats", ChatColor.GRAY + "Click to edit"));
        inv.setItem(15, item(Material.ENCHANTED_BOOK, ChatColor.YELLOW + "Set Bonus"));
        inv.setItem(16, item(Material.ANVIL, ChatColor.GREEN + "Create"));
        inv.setItem(26, item(Material.BARRIER, ChatColor.RED + "Back"));
        player.openInventory(inv);
    }

    /** Open the list of created items. */
    private void openList(Player player) {
        int size = ((createdItems.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(player, Math.max(9, Math.min(54, size)), ChatColor.DARK_BLUE + "Created Items");
        for (ItemStack it : createdItems) inv.addItem(it);
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

    private void openSetBonus(Player player) {
        Builder b = builders.get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_PURPLE + "Set Bonus");
        inv.setItem(10, item(Material.PAPER, ChatColor.YELLOW + "Set Name", b.setName == null ? ChatColor.GRAY + "None" : b.setName));
        inv.setItem(11, item(Material.LEATHER_CHESTPLATE, ChatColor.YELLOW + "Pieces", ChatColor.GRAY + b.pieces.size() + " selected"));
        inv.setItem(12, item(Material.PAPER, ChatColor.AQUA + "2 Pieces"));
        inv.setItem(13, item(Material.PAPER, ChatColor.AQUA + "4 Pieces"));
        inv.setItem(14, item(Material.PAPER, ChatColor.AQUA + "6 Pieces"));
        inv.setItem(26, item(Material.ARROW, ChatColor.YELLOW + "Back"));
        player.openInventory(inv);
    }

    private void openPieces(Player player) {
        Builder b = builders.get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 27, ChatColor.DARK_BLUE + "Select Pieces");
        int i = 10;
        for (ArmorPiece p : ArmorPiece.values()) {
            boolean sel = b.pieces.contains(p);
            inv.setItem(i++, item(p.getMaterial(), (sel ? ChatColor.GREEN : ChatColor.GRAY) + p.getName()));
        }
        inv.setItem(15, item(Material.ANVIL, ChatColor.GREEN + "Done"));
        inv.setItem(26, item(Material.ARROW, ChatColor.YELLOW + "Back"));
        player.openInventory(inv);
    }

    private void openPieceStats(Player player, int pieces) {
        Builder b = builders.get(player.getUniqueId());
        PlayerStats stats = b.setBonuses.getOrDefault(pieces, new PlayerStats());
        b.editingPiece = pieces;
        Inventory inv = Bukkit.createInventory(player, 54, ChatColor.BLUE + pieces + " Piece Stats");
        inv.setItem(10, statItem("Strength", stats.strength));
        inv.setItem(11, statItem("Dexterity", stats.dexterity));
        inv.setItem(12, statItem("Defense", stats.defense));
        inv.setItem(13, statItem("Max Mana", stats.maxMana));
        inv.setItem(14, statItem("Mana Regen", stats.manaRegen));
        inv.setItem(15, statItem("Ability Damage", stats.abilityDamage));
        inv.setItem(16, statItem("Agility", stats.agility));
        inv.setItem(49, item(Material.ARROW, ChatColor.YELLOW + "Back"));
        player.openInventory(inv);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> cb = chatWait.remove(event.getPlayer().getUniqueId());
        if (cb != null) {
            event.setCancelled(true);
            cb.accept(ChatColor.stripColor(event.getMessage()));
        }
    }

    private ItemStack statItem(String name, int value) {
        return item(Material.PAPER, ChatColor.AQUA + name,
                ChatColor.GRAY + "Value: " + value,
                ChatColor.YELLOW + "Left +1, Right -1",
                ChatColor.YELLOW + "Shift = +/-5");
    }

    private void create(Player player) {
        Builder b = builders.get(player.getUniqueId());
        int count = b.pieces.isEmpty() ? 1 : b.pieces.size();
        PlayerStats per = count > 1 ? divideStats(b.stats, count) : b.stats;

        List<ItemStack> items = new ArrayList<>();
        if (b.pieces.isEmpty()) {
            ItemStack item = b.base == null ? new ItemStack(Material.DIAMOND_CHESTPLATE) : b.base.clone();
            applyMeta(item, b);
            if (b.ability != null) abilityManager.addAbility(item, b.ability);
            statsManager.applyItemStats(item, per);
            if (b.setName != null) statsManager.applySetInfo(item, b.setName, b.setBonuses);
            items.add(item);
        } else {
            for (ArmorPiece p : b.pieces) {
                ItemStack item = new ItemStack(p.getMaterial());
                applyMeta(item, b);
                if (b.ability != null) abilityManager.addAbility(item, b.ability);
                statsManager.applyItemStats(item, per);
                if (b.setName != null) statsManager.applySetInfo(item, b.setName, b.setBonuses);
                items.add(item);
            }
        }

        for (ItemStack it : items) {
            player.getInventory().addItem(it);
            createdItems.add(it.clone());
        }
        builders.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Created custom item.");
        player.closeInventory();
    }

    private void applyMeta(ItemStack item, Builder b) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String baseName = b.name != null ? b.name : meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "Custom Item";
        meta.setDisplayName(b.rarity.format(baseName));
        item.setItemMeta(meta);
    }

    private PlayerStats divideStats(PlayerStats s, int n) {
        PlayerStats out = new PlayerStats();
        out.strength = s.strength / n;
        out.dexterity = s.dexterity / n;
        out.defense = s.defense / n;
        out.maxMana = s.maxMana / n;
        out.manaRegen = s.manaRegen / n;
        out.abilityDamage = s.abilityDamage / n;
        out.agility = s.agility / n;
        return out;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Builder b = builders.get(player.getUniqueId());
        if (b != null && b.selectingBase && e.getClickedInventory() == player.getInventory()) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it != null) {
                b.base = it.clone();
            }
            b.selectingBase = false;
            openCreator(player);
            return;
        }

        if (title.equals(ChatColor.DARK_AQUA + "Custom Items")) {
            e.setCancelled(true);
            if (e.getRawSlot() == 11) openCreator(player);
            else if (e.getRawSlot() == 15) openList(player);
        } else if (title.equals(ChatColor.DARK_AQUA + "Item Creator")) {
            e.setCancelled(true);
            switch (e.getRawSlot()) {
                case 9 -> { b.selectingBase = true; player.sendMessage(ChatColor.YELLOW + "Click an item in your inventory."); }
                case 10 -> openAbility(player,0);
                case 11 -> {
                    player.closeInventory();
                    chatWait.put(player.getUniqueId(), msg -> { b.name = msg; openCreator(player); });
                    player.sendMessage(ChatColor.YELLOW + "Type item name in chat.");
                }
                case 12 -> openRarity(player);
                case 13 -> openPieces(player);
                case 14 -> openStats(player);
                case 15 -> openSetBonus(player);
                case 16 -> create(player);
                case 26 -> { builders.remove(player.getUniqueId()); open(player); }
            }
        } else if (title.equals(ChatColor.DARK_BLUE + "Created Items")) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it != null) player.getInventory().addItem(it.clone());
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
            openCreator(player);
        } else if (title.equals(ChatColor.DARK_PURPLE + "Select Rarity")) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            try {
                b.rarity = Rarity.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            openCreator(player);
        } else if (title.equals(ChatColor.BLUE + "Edit Stats")) {
            e.setCancelled(true);
            if (e.getSlot() == 49) { openCreator(player); return; }
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            String stat = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            int delta = e.isShiftClick() ? 5 : 1;
            if (e.isRightClick()) delta = -delta;
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
        } else if (title.equals(ChatColor.DARK_PURPLE + "Set Bonus")) {
            e.setCancelled(true);
            switch (e.getRawSlot()) {
                case 10 -> {
                    player.closeInventory();
                    chatWait.put(player.getUniqueId(), msg -> { b.setName = msg; openSetBonus(player); });
                    player.sendMessage(ChatColor.YELLOW + "Type set name in chat.");
                }
                case 11 -> openPieces(player);
                case 12 -> openPieceStats(player,2);
                case 13 -> openPieceStats(player,4);
                case 14 -> openPieceStats(player,6);
                case 26 -> openCreator(player);
            }
        } else if (title.equals(ChatColor.DARK_BLUE + "Select Pieces")) {
            e.setCancelled(true);
            if (e.getRawSlot() == 26) { openSetBonus(player); return; }
            if (e.getRawSlot() == 15) { openSetBonus(player); return; }
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            int slotIndex = e.getRawSlot() - 10;
            if (slotIndex >= 0 && slotIndex < ArmorPiece.values().length) {
                ArmorPiece p = ArmorPiece.values()[slotIndex];
                if (b.pieces.contains(p)) b.pieces.remove(p); else if (b.pieces.size() < 4) b.pieces.add(p);
                openPieces(player);
            }
        } else if (title.endsWith(" Piece Stats")) {
            e.setCancelled(true);
            if (e.getSlot() == 49) { openSetBonus(player); return; }
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            String stat = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            int delta = e.isShiftClick() ? 5 : 1;
            if (e.isRightClick()) delta = -delta;
            PlayerStats ps = b.setBonuses.getOrDefault(b.editingPiece, new PlayerStats());
            switch (stat) {
                case "Strength" -> ps.strength = Math.max(0, ps.strength + delta);
                case "Dexterity" -> ps.dexterity = Math.max(0, ps.dexterity + delta);
                case "Defense" -> ps.defense = Math.max(0, ps.defense + delta);
                case "Max Mana" -> ps.maxMana = Math.max(0, ps.maxMana + delta);
                case "Mana Regen" -> ps.manaRegen = Math.max(0, ps.manaRegen + delta);
                case "Ability Damage" -> ps.abilityDamage = Math.max(0, ps.abilityDamage + delta);
                case "Agility" -> ps.agility = Math.max(0, ps.agility + delta);
            }
            b.setBonuses.put(b.editingPiece, ps);
            openPieceStats(player, b.editingPiece);
        }
    }

    private static class Builder {
        Ability ability;
        Rarity rarity = Rarity.COMMON;
        PlayerStats stats = new PlayerStats();
        ItemStack base;
        String name;
        String setName;
        Map<Integer, PlayerStats> setBonuses = new HashMap<>();
        java.util.Set<ArmorPiece> pieces = java.util.EnumSet.noneOf(ArmorPiece.class);
        boolean selectingBase = false;
        int editingPiece = 0;
    }
}
