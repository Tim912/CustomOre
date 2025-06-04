package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.customitemsystem.stats.StatsManager;
import com.example.customitemsystem.stats.PlayerStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple mana system that regenerates over time and is displayed
 * in the player's action bar.
 */
public class ManaManager implements Listener {
    private final Map<UUID, Integer> mana = new HashMap<>();
    private final JavaPlugin plugin;
    private final StatsManager statsManager;
    private final int baseMaxMana = 100;

    public ManaManager(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 20L);
    }

    private void tick() {
        for (UUID id : mana.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            PlayerStats s = statsManager.getStats(p);
            int max = baseMaxMana + s.maxMana;
            int regen = 1 + s.manaRegen;
            int value = Math.min(max, mana.get(id) + regen);
            mana.put(id, value);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.AQUA + "Mana: " + value + "/" + max));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerStats s = statsManager.getStats(event.getPlayer());
        mana.put(event.getPlayer().getUniqueId(), baseMaxMana + s.maxMana);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        mana.remove(event.getPlayer().getUniqueId());
    }

    public boolean useMana(Player player, int amount) {
        UUID id = player.getUniqueId();
        PlayerStats s = statsManager.getStats(player);
        int max = baseMaxMana + s.maxMana;
        int current = mana.getOrDefault(id, max);
        if (current < amount) {
            player.sendMessage(ChatColor.RED + "Not enough mana!");
            return false;
        }
        mana.put(id, current - amount);
        return true;
    }
}
