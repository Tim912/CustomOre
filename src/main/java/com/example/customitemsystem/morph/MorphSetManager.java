package com.example.customitemsystem.morph;

import java.util.EnumSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.customitemsystem.stats.StatsManager;
import com.example.customitemsystem.stats.PlayerStats;

/**
 * Applies bonuses for players wearing Morph set items.
 */
public class MorphSetManager implements Listener {
    private final JavaPlugin plugin;
    private final StatsManager statsManager;

    public MorphSetManager(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 0L, 100L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int pieces = countPieces(player);
            statsManager.setMorphBonus(player, pieces == MorphItem.values().length);
            if (pieces == MorphItem.values().length) {
                applyFullSet(player);
            }
            updateLore(player, pieces);
        }
    }

    private void updateLore(Player player, int pieces) {
        for (ItemStack item : player.getInventory().getArmorContents()) {
            refreshItemLore(item, pieces);
        }
        for (ItemStack item : player.getInventory().getContents()) {
            refreshItemLore(item, pieces);
        }
    }

    /** Updates Morph item lore for the given player immediately. */
    public void refreshPlayer(Player player) {
        updateLore(player, countPieces(player));
    }

    private void refreshItemLore(ItemStack item, int pieces) {
        if (item == null) return;
        MorphItem morph = MorphItem.fromMaterial(item.getType());
        if (morph == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Requires Level " + morph.getLevelReq());
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + "Morph Set Bonuses:");

        String[] bonus = {
            "+5 All Skills",
            "+8 Mana Regeneration /5s",
            "+20% Walk Speed",
            "+260 Health Regeneration",
            "+182 Spell Damage",
            "+176 Melee Damage"
        };
        int[] thresh = {2,4,6,8,8,8};
        boolean addedNext = false;
        for (int i = 0; i < bonus.length; i++) {
            if (pieces >= thresh[i]) {
                lore.add(ChatColor.AQUA + bonus[i]);
            } else if (!addedNext) {
                lore.add(ChatColor.DARK_GRAY + bonus[i]);
                addedNext = true;
            }
        }

        lore.add("");
        lore.add(ChatColor.GREEN + "Pieces: " + pieces + "/" + MorphItem.values().length);
        int next = nextThreshold(pieces);
        if (next > pieces) {
            lore.add(ChatColor.GRAY + "Next bonus at " + next + " pieces");
        } else {
            lore.add(ChatColor.GRAY + "All bonuses unlocked");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private int nextThreshold(int pieces) {
        int[] t = {2,4,6,8};
        for (int x : t) {
            if (pieces < x) return x;
        }
        return 8;
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
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() + 4
        );
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
