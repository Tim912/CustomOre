package com.example.customitemsystem.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tracks player stats from various sources.
 */
public class StatsManager {
    private final Map<UUID, PlayerStats> morphBonus = new HashMap<>();
    private final NamespacedKey statsKey;
    private final NamespacedKey setKey;
    private final NamespacedKey bonusKey;

    public StatsManager(JavaPlugin plugin) {
        this.statsKey = new NamespacedKey(plugin, "item_stats");
        this.setKey = new NamespacedKey(plugin, "set_name");
        this.bonusKey = new NamespacedKey(plugin, "set_bonus");
    }

    /** Enable or disable the Morph set stat bonus. */
    public void setMorphBonus(Player player, boolean active) {
        if (active) {
            morphBonus.put(player.getUniqueId(), PlayerStats.of(5));
        } else {
            morphBonus.remove(player.getUniqueId());
        }
    }

    /** Retrieve the total stats for a player. */
    public PlayerStats getStats(Player player) {
        PlayerStats total = new PlayerStats();
        PlayerStats morph = morphBonus.get(player.getUniqueId());
        if (morph != null) total.add(morph);

        Map<String, Set<Material>> pieces = new HashMap<>();
        Map<String, Map<Integer, PlayerStats>> bonuses = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            addItemStats(item, total);
            collectSetData(item, pieces, bonuses);
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            addItemStats(item, total);
            collectSetData(item, pieces, bonuses);
        }

        // apply set bonuses
        for (Map.Entry<String, Set<Material>> e : pieces.entrySet()) {
            String name = e.getKey();
            int count = e.getValue().size();
            Map<Integer, PlayerStats> map = bonuses.get(name);
            if (map == null) continue;
            int best = 0;
            for (int req : map.keySet()) {
                if (count >= req && req > best) best = req;
            }
            PlayerStats bonus = map.get(best);
            if (bonus != null) total.add(bonus);
        }

        return total;
    }

    private void addItemStats(ItemStack item, PlayerStats target) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer c = meta.getPersistentDataContainer();
        String data = c.get(statsKey, PersistentDataType.STRING);
        if (data != null) target.add(decode(data));
    }

    private void collectSetData(ItemStack item, Map<String, Set<Material>> pieces, Map<String, Map<Integer, PlayerStats>> bonuses) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer c = meta.getPersistentDataContainer();
        String name = c.get(setKey, PersistentDataType.STRING);
        if (name == null) return;
        pieces.computeIfAbsent(name, k -> new java.util.HashSet<>()).add(item.getType());
        if (!bonuses.containsKey(name)) {
            String encoded = c.get(bonusKey, PersistentDataType.STRING);
            if (encoded != null) bonuses.put(name, decodeBonuses(encoded));
        }
    }

    public void applyItemStats(ItemStack item, PlayerStats stats) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(statsKey, PersistentDataType.STRING, encode(stats));
        java.util.List<String> lore = meta.getLore() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(meta.getLore());
        lore.add("" + ChatColor.GOLD + "Stats:");
        lore.add(ChatColor.YELLOW + "Strength: " + stats.strength);
        lore.add(ChatColor.YELLOW + "Dexterity: " + stats.dexterity);
        lore.add(ChatColor.YELLOW + "Defense: " + stats.defense);
        lore.add(ChatColor.YELLOW + "Max Mana: " + stats.maxMana);
        lore.add(ChatColor.YELLOW + "Mana Regen: " + stats.manaRegen);
        lore.add(ChatColor.YELLOW + "Ability Damage: " + stats.abilityDamage);
        lore.add(ChatColor.YELLOW + "Agility: " + stats.agility);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void applySetInfo(ItemStack item, String name, Map<Integer, PlayerStats> bonuses) {
        if (item == null || name == null || name.isEmpty()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(setKey, PersistentDataType.STRING, name);
        meta.getPersistentDataContainer().set(bonusKey, PersistentDataType.STRING, encodeBonuses(bonuses));
        java.util.List<String> lore = meta.getLore() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(meta.getLore());
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + name + " Set");
        for (Map.Entry<Integer, PlayerStats> e : bonuses.entrySet()) {
            PlayerStats s = e.getValue();
            lore.add(ChatColor.GRAY + "" + e.getKey() + " pieces: +" + s.strength + " STR");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private String encode(PlayerStats s) {
        return s.strength + "|" + s.dexterity + "|" + s.defense + "|" + s.maxMana + "|" + s.manaRegen + "|" + s.abilityDamage + "|" + s.agility;
    }

    private PlayerStats decode(String d) {
        String[] p = d.split("\\|");
        PlayerStats s = new PlayerStats();
        if (p.length > 0) s.strength = Integer.parseInt(p[0]);
        if (p.length > 1) s.dexterity = Integer.parseInt(p[1]);
        if (p.length > 2) s.defense = Integer.parseInt(p[2]);
        if (p.length > 3) s.maxMana = Integer.parseInt(p[3]);
        if (p.length > 4) s.manaRegen = Integer.parseInt(p[4]);
        if (p.length > 5) s.abilityDamage = Integer.parseInt(p[5]);
        if (p.length > 6) s.agility = Integer.parseInt(p[6]);
        return s;
    }

    private String encodeBonuses(Map<Integer, PlayerStats> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, PlayerStats> e : map.entrySet()) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.getKey()).append(':').append(encode(e.getValue()));
        }
        return sb.toString();
    }

    private Map<Integer, PlayerStats> decodeBonuses(String s) {
        Map<Integer, PlayerStats> map = new HashMap<>();
        if (s == null || s.isEmpty()) return map;
        for (String part : s.split(";")) {
            String[] kv = part.split(":");
            if (kv.length != 2) continue;
            map.put(Integer.parseInt(kv[0]), decode(kv[1]));
        }
        return map;
    }

    /** Send a chat overview of all stats to the player. */
    public void showStats(Player player) {
        PlayerStats s = getStats(player);
        player.sendMessage(ChatColor.GOLD + "---- Skills ----");
        player.sendMessage(ChatColor.YELLOW + "Strength: " + s.strength + ChatColor.GRAY + " (increases melee damage)");
        player.sendMessage(ChatColor.YELLOW + "Dexterity: " + s.dexterity + ChatColor.GRAY + " (improves crit chance)");
        player.sendMessage(ChatColor.YELLOW + "Defense: " + s.defense + ChatColor.GRAY + " (reduces damage taken)");
        player.sendMessage(ChatColor.YELLOW + "Max Mana: " + s.maxMana + ChatColor.GRAY + " (extra maximum mana)");
        player.sendMessage(ChatColor.YELLOW + "Mana Regen: " + s.manaRegen + ChatColor.GRAY + " (extra mana per tick)");
        player.sendMessage(ChatColor.YELLOW + "Ability Damage: " + s.abilityDamage + ChatColor.GRAY + " (boosts ability power)");
        player.sendMessage(ChatColor.YELLOW + "Agility: " + s.agility + ChatColor.GRAY + " (movement speed)");
    }
}
