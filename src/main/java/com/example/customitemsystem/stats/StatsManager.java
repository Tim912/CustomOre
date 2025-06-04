package com.example.customitemsystem.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Tracks player stats from various sources.
 */
public class StatsManager {
    private final Map<UUID, PlayerStats> morphBonus = new HashMap<>();

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
        return total;
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
