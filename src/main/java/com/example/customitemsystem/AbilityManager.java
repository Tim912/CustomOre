package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.customitemsystem.ManaManager;
import com.example.customitemsystem.stats.StatsManager;
import com.example.customitemsystem.stats.PlayerStats;

import java.util.ArrayList;
import java.util.List;

public class AbilityManager implements Listener {

    private final NamespacedKey key;
    private final JavaPlugin plugin;
    private final ManaManager manaManager;
    private final StatsManager statsManager;

    public AbilityManager(JavaPlugin plugin, ManaManager manaManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.manaManager = manaManager;
        this.statsManager = statsManager;
        this.key = new NamespacedKey(plugin, "abilities");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void addAbility(ItemStack item, Ability ability) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<String> list = getAbilities(meta);
        if (!list.contains(ability.name())) {
            list.add(ability.name());
            container.set(key, PersistentDataType.STRING, String.join(",", list));
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add(ability.getDisplayName());
            lore.add(ability.getDescription());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    public List<String> getAbilities(ItemMeta meta) {
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String data = container.get(key, PersistentDataType.STRING);
        List<String> list = new ArrayList<>();
        if (data != null && !data.isEmpty()) {
            for (String s : data.split(",")) {
                list.add(s);
            }
        }
        return list;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> abilities = getAbilities(meta);
        if (abilities.isEmpty()) return;
        Player player = event.getPlayer();
        for (String ab : abilities) {
            Ability ability = Ability.fromString(ab);
            if (ability != null) {
                if (manaManager.useMana(player, ability.getCost())) {
                    applyAbility(player, ability);
                }
            }
        }
    }

    private void applyAbility(Player player, Ability ability) {
        switch (ability) {
            case FIREBALL -> player.launchProjectile(org.bukkit.entity.Fireball.class);
            case HEAL -> player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 4));
            case SPEED_BOOST -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 200, 1));
            case INVISIBILITY -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, 200, 0));
            case STRENGTH -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 200, 1));
            case JUMP -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.JUMP, 200, 2));
            case LIGHTNING -> player.getWorld().strikeLightning(player.getTargetBlockExact(50).getLocation());
            case TELEPORT -> {
                org.bukkit.Location target = player.getLocation().add(player.getLocation().getDirection().multiply(5));
                if (!target.getBlock().getType().isSolid() && !target.clone().add(0,1,0).getBlock().getType().isSolid()) {
                    player.teleport(target);
                }
            }
            case EXPLOSION -> player.getWorld().createExplosion(player.getLocation(), 2F, false, false);
            case FLIGHT -> player.setAllowFlight(true);
            case WATER_BREATHING -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 600, 0));
            case RESISTANCE -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 200, 1));
            case HASTE -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.FAST_DIGGING, 200, 1));
            case REGENERATION -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 200, 1));
            case NIGHT_VISION -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, 400, 0));
            case FIRE_RESISTANCE -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 600, 0));
            case LUCK -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LUCK, 600, 0));
            case GLOWING -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 200, 0));
            case POISON_CLOUD -> player.getWorld().spawnParticle(org.bukkit.Particle.SPELL_MOB, player.getLocation(), 20, 0.5, 0.5, 0.5, 1);
            case SLOWNESS_AREA -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW, 200, 1));
            case HYPERION_BEAM -> {
                org.bukkit.Location loc = player.getEyeLocation();
                for (int i = 0; i < 5; i++) {
                    loc = loc.add(loc.getDirection());
                    player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, loc, 10, 0.1, 0.1, 0.1, 0);
                    player.getWorld().createExplosion(loc, 1F, false, false);
                }
            }
            case TERMINATOR_VOLLEY -> {
                org.bukkit.Location base = player.getEyeLocation();
                org.bukkit.util.Vector side = base.getDirection().clone().crossProduct(new org.bukkit.util.Vector(0,1,0)).normalize();
                for (int i = -1; i <= 1; i++) {
                    org.bukkit.Location loc = base.clone().add(side.clone().multiply(i));
                    org.bukkit.entity.Arrow arrow = player.getWorld().spawnArrow(loc, base.getDirection(), 2F, 0F);
                    arrow.setShooter(player);
                }
            }
            case VOID_SLASH -> {
                org.bukkit.Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(4));
                if (!loc.getBlock().getType().isSolid() && !loc.clone().add(0,1,0).getBlock().getType().isSolid()) {
                    player.teleport(loc);
                }
                player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, loc, 10, 0.5, 0.5, 0.5);
            }
            case FROZEN_SCYTHE -> {
                org.bukkit.entity.Snowball snow = player.launchProjectile(org.bukkit.entity.Snowball.class);
                snow.setVelocity(player.getLocation().getDirection().multiply(1.5));
                int ad = statsManager.getStats(player).abilityDamage;
                snow.setMetadata("abilityDamage", new org.bukkit.metadata.FixedMetadataValue(plugin, ad));
            }
            case LIGHTBINDER -> {
                player.setAbsorptionAmount(player.getAbsorptionAmount() + 6);
            }
            case SHADOW_RUSH -> {
                org.bukkit.Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(6));
                if (!loc.getBlock().getType().isSolid() && !loc.clone().add(0,1,0).getBlock().getType().isSolid()) {
                    player.teleport(loc);
                }
                player.spawnParticle(org.bukkit.Particle.SMOKE_LARGE, player.getLocation(), 10, 0.5,0.5,0.5,0);
            }
            case AETHER_SHIELD -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 200, 2));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.hasMetadata("abilityDamage")) {
            int bonus = proj.getMetadata("abilityDamage").get(0).asInt();
            event.setDamage(event.getDamage() + bonus);
        }
    }
}
