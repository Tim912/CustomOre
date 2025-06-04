package com.example.customitemsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AbilityManager implements Listener {

    private final NamespacedKey key;
    private final JavaPlugin plugin;

    public AbilityManager(JavaPlugin plugin) {
        this.plugin = plugin;
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
                applyAbility(player, ability);
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
            case TELEPORT -> player.teleport(player.getLocation().add(player.getLocation().getDirection().multiply(5)));
            case TRIPLE_SHOT -> {
                for (int i = 0; i < 3; i++) {
                    player.launchProjectile(org.bukkit.entity.Arrow.class);
                }
            }
            case ARROW_RAIN -> {
                for (int i = 0; i < 12; i++) {
                    org.bukkit.entity.Arrow arrow = player.getWorld().spawnArrow(player.getLocation().add(0, 2, 0), new org.bukkit.util.Vector(Math.random() - 0.5, 0, Math.random() - 0.5), 1F, 12F);
                    arrow.setShooter(player);
                }
            }
            case HYPERION_STRIKE -> {
                player.teleport(player.getLocation().add(player.getLocation().getDirection().multiply(8)));
                player.getWorld().createExplosion(player.getLocation(), 4F, false, false);
            }
            case BLIZZARD -> player.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, player.getLocation(), 50, 2, 2, 2, 0.1);
            case FORTUNE_MINER -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LUCK, 600, 1));
            case SHADOW_STEP -> player.teleport(player.getLocation().add(player.getLocation().getDirection().multiply(3)));
            case EARTHQUAKE -> {
                player.getWorld().spawnParticle(org.bukkit.Particle.BLOCK_CRACK, player.getLocation(), 30, 1, 0.5, 1, org.bukkit.Material.DIRT.createBlockData());
                player.getNearbyEntities(3, 3, 3).stream().filter(e -> e instanceof org.bukkit.entity.LivingEntity).forEach(e -> ((org.bukkit.entity.LivingEntity) e).damage(4, player));
            }
            case WITHER_SKULL -> player.launchProjectile(org.bukkit.entity.WitherSkull.class);
            case BLINK -> player.teleport(player.getLocation().add(player.getLocation().getDirection().multiply(5)));
            case SONIC_BOOM -> player.getNearbyEntities(3, 3, 3).forEach(e -> e.setVelocity(e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5)));
            case MANA_SHIELD -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION, 200, 1));
            case BERSERK -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 200, 2));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS, 200, 0));
            }
        }
    }
}
