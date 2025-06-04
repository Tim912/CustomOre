package com.example.customitemsystem.slayer;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

/**
 * Types of slayer bosses.
 */
public enum BossType {
    ZOMBIE_LORD("Zombie Lord", EntityType.ZOMBIE, EntityType.ZOMBIE),
    NECRO_MAGE("Necro Mage", EntityType.SKELETON, EntityType.SKELETON),
    WITHER_KING("Wither King", EntityType.WITHER_SKELETON, EntityType.WITHER_SKELETON);

    private final String displayName;
    private final EntityType entityType;
    private final EntityType killType;

    BossType(String displayName, EntityType entityType, EntityType killType) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.killType = killType;
    }

    public String getDisplayName() {
        return ChatColor.RED + displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public EntityType getKillType() {
        return killType;
    }
}
