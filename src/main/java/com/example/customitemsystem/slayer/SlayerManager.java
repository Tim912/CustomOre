package com.example.customitemsystem.slayer;

import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles slayer quests and menus.
 */
public class SlayerManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, SlayerQuest> quests = new HashMap<>();

    public SlayerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9,
                Component.text("Slayer", NamedTextColor.DARK_RED));
        for (BossType type : BossType.values()) {
            ItemStack item;
            switch (type) {
                case ZOMBIE_LORD -> item = new ItemStack(Material.ROTTEN_FLESH);
                case NECRO_MAGE -> item = new ItemStack(Material.BONE);
                case WITHER_KING -> item = new ItemStack(Material.NETHER_STAR);
                default -> item = new ItemStack(Material.STONE);
            }
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(type.getDisplayName());
                item.setItemMeta(meta);
            }
            inv.addItem(item);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_RED + "Slayer")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            String name = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
            for (BossType type : BossType.values()) {
                if (type.getDisplayName().contains(name)) {
                    openTierMenu(player, type);
                    return;
                }
            }
        } else if (event.getView().getTitle().startsWith(ChatColor.RED + "Select")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;
            Player player = (Player) event.getWhoClicked();
            String title = ChatColor.stripColor(event.getView().getTitle());
            BossType type = BossType.valueOf(title.split(" ")[1]);
            int tier = event.getSlot() + 1;
            startQuest(player, type, tier);
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Started " + type.getDisplayName() + " Tier " + tier);
        }
    }

    private void openTierMenu(Player player, BossType type) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text("Select " + type.name(), NamedTextColor.RED));
        for (int i = 1; i <= 5; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Tier " + i);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Kill " + (i * 10) + " " + type.getKillType().name());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i - 1, item);
        }
        player.openInventory(inv);
    }

    private void startQuest(Player player, BossType type, int tier) {
        int killsNeeded = tier * 10;
        BossBar bar = Bukkit.createBossBar(Component.text("Kills", NamedTextColor.DARK_RED),
                BarColor.RED, BarStyle.SEGMENTED_10);
        bar.addPlayer(player);
        quests.put(player.getUniqueId(), new SlayerQuest(player, type, tier, killsNeeded, type.getKillType(), bar));
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        SlayerQuest quest = quests.get(killer.getUniqueId());
        if (quest == null) return;
        if (event.getEntity().getType() == quest.getKillType()) {
            quest.addKill();
            double progress = (double) quest.getKills() / quest.getKillsNeeded();
            quest.getBar().setProgress(Math.min(1.0, progress));
            killer.sendMessage(ChatColor.GRAY + "Kills: " + quest.getKills() + "/" + quest.getKillsNeeded());
            if (quest.isComplete()) {
                quest.getBar().removeAll();
                spawnBoss(quest);
                quests.remove(killer.getUniqueId());
            }
        }
    }

    private void spawnBoss(SlayerQuest quest) {
        Player player = quest.getPlayer();
        LivingEntity entity = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(),
                quest.getBossType().getEntityType());
        entity.customName(LegacyComponentSerializer.legacySection()
                .deserialize(quest.getBossType().getDisplayName() + ChatColor.WHITE + " Tier " + quest.getTier()));
        entity.setCustomNameVisible(true);
        double hp = 20 + quest.getTier() * 10;
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
        entity.setHealth(hp);
        final BossBar bar = Bukkit.createBossBar(entity.getCustomName(), BarColor.PURPLE, BarStyle.SEGMENTED_10);
        bar.addPlayer(player);
        bar.setProgress(1.0);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (entity.isDead()) {
                    bar.removeAll();
                    cancel();
                    return;
                }
                bar.setProgress(entity.getHealth() / entity.getMaxHealth());
                ticks += 20;
                switch (quest.getBossType()) {
                    case ZOMBIE_LORD -> {
                        if (ticks % 100 == 0) {
                            player.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
                        }
                        if (ticks % 120 == 0) {
                            entity.setVelocity(player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.5));
                        }
                    }
                    case NECRO_MAGE -> {
                        if (ticks % 80 == 0) {
                            entity.launchProjectile(org.bukkit.entity.Fireball.class);
                        }
                        if (ticks % 160 == 0) {
                            player.getWorld().strikeLightning(player.getLocation());
                        }
                    }
                    case WITHER_KING -> {
                        if (ticks % 120 == 0) {
                            entity.teleport(player.getLocation());
                            player.getWorld().createExplosion(entity.getLocation(), 2F, false, false);
                        }
                        if (ticks % 100 == 0) {
                            player.getWorld().spawnEntity(entity.getLocation(), EntityType.WITHER_SKELETON);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
