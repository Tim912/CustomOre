package com.example.customitemsystem.slayer;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;

/**
 * Types of slayer bosses.
 */
public enum BossType {
    ZOMBIE_LORD("Zombie Lord", EntityType.ZOMBIE),
    NECRO_MAGE("Necro Mage", EntityType.SKELETON),
    WITHER_KING("Wither King", EntityType.WITHER_SKELETON);

    private final String displayName;
    private final EntityType entityType;

    BossType(String displayName, EntityType entityType) {
        this.displayName = displayName;
        this.entityType = entityType;
    }

    public String getDisplayName() {
        return ChatColor.RED + displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
