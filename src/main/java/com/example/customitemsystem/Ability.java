package com.example.customitemsystem;

import org.bukkit.ChatColor;

/**
 * Enumeration of all custom item abilities. Each entry contains
 * a colored display name and a short description that will be
 * added to the item's lore.
 */
public enum Ability {
    FIREBALL("Fireball", "Shoots a fireball", 10),
    HEAL("Heal", "Restores some health", 8),
    SPEED_BOOST("Speed Boost", "Temporary speed increase", 6),
    INVISIBILITY("Invisibility", "Become invisible for a short time", 12),
    STRENGTH("Strength", "Gain extra damage", 10),
    JUMP("Jump", "Leap high into the air", 4),
    LIGHTNING("Lightning", "Strike lightning", 15),
    TELEPORT("Teleport", "Teleport a few blocks forward", 5),
    EXPLOSION("Explosion", "Create a small explosion", 14),
    FLIGHT("Flight", "Toggle flight", 2),
    WATER_BREATHING("Water Breathing", "Breathe underwater", 2),
    RESISTANCE("Resistance", "Take less damage", 8),
    HASTE("Haste", "Faster digging", 6),
    REGENERATION("Regeneration", "Regenerate health", 8),
    NIGHT_VISION("Night Vision", "See in the dark", 2),
    FIRE_RESISTANCE("Fire Resistance", "Immune to fire", 2),
    LUCK("Luck", "Increased luck", 2),
    GLOWING("Glowing", "Become glowing", 2),
    POISON_CLOUD("Poison Cloud", "Emit a poison cloud", 10),
    SLOWNESS_AREA("Slowness Area", "Slow nearby foes", 10),
    HYPERION_BEAM("Hyperion Beam", "Fires an explosive beam", 20),
    TERMINATOR_VOLLEY("Terminator Volley", "Shoots three arrows", 18),
    VOID_SLASH("Void Slash", "Dash forward with a swipe", 12),
    FROZEN_SCYTHE("Frozen Scythe", "Unleash icy bolts", 16),
    LIGHTBINDER("Lightbinder", "Gain absorption hearts", 14),
    SHADOW_RUSH("Shadow Rush", "Dash through enemies", 15),
    AETHER_SHIELD("Aether Shield", "Temporary shield of mana", 12);

    private final String displayName;
    private final String description;
    private final int cost;

    Ability(String displayName, String description, int cost) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
    }

    public String getDisplayName() {
        return ChatColor.GREEN + displayName;
    }

    public String getDescription() {
        return ChatColor.GRAY + description;
    }

    public int getCost() {
        return cost;
    }

    public static Ability fromString(String name) {
        for (Ability a : values()) {
            if (a.name().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }
}
