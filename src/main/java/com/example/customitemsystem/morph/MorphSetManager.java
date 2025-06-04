package com.example.customitemsystem.morph;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Applies bonuses for players wearing Morph set items.
 */
public class MorphSetManager implements Listener {
    private final JavaPlugin plugin;

    public MorphSetManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 100L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int pieces = countPieces(player);
            if (pieces == MorphItem.values().length) {
                applyFullSet(player);
            }
        }
    }

    private int countPieces(Player player) {
        int count = 0;
        ItemStack[] armor = player.getInventory().getArmorContents();
        if (armor[3] != null && armor[3].getType() == MorphItem.MORPH_STARDUST.getMaterial()) count++;
        if (armor[2] != null && armor[2].getType() == MorphItem.MORPH_STEEL.getMaterial()) count++;
        if (armor[1] != null && armor[1].getType() == MorphItem.MORPH_IRON.getMaterial()) count++;
        if (armor[0] != null && armor[0].getType() == MorphItem.MORPH_GOLD.getMaterial()) count++;
        Set<Material> acc = EnumSet.noneOf(Material.class);
        acc.add(MorphItem.MORPH_TOPAZ.getMaterial());
        acc.add(MorphItem.MORPH_EMERALD.getMaterial());
        acc.add(MorphItem.MORPH_AMETHYST.getMaterial());
        acc.add(MorphItem.MORPH_RUBY.getMaterial());
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (acc.remove(item.getType())) count++;
        }
        return count;
    }

    private void applyFullSet(Player player) {
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 120, 2));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 120, 1));
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue( player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() + 4);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Morph set bonuses active when all pieces are equipped.");
    }

    // Example bonus: extra damage when attacking while full set is equipped
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (countPieces(player) == MorphItem.values().length) {
                event.setDamage(event.getDamage() + 5);
            }
        }
    }
}
