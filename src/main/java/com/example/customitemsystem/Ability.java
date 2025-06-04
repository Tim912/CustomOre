package com.example.customitemsystem;

import org.bukkit.ChatColor;

/**
 * Enumeration of all custom item abilities. Each entry contains
 * a colored display name and a short description that will be
 * added to the item's lore.
 */
public enum Ability {
    FIREBALL("Fireball", "Shoots a fireball"),
    HEAL("Heal", "Restores some health"),
    SPEED_BOOST("Speed Boost", "Temporary speed increase"),
    INVISIBILITY("Invisibility", "Become invisible for a short time"),
    STRENGTH("Strength", "Gain extra damage"),
    JUMP("Jump", "Leap high into the air"),
    LIGHTNING("Lightning", "Strike lightning"),
    TELEPORT("Teleport", "Teleport a few blocks forward"),
    EXPLOSION("Explosion", "Create a small explosion"),
    FLIGHT("Flight", "Toggle flight"),
    WATER_BREATHING("Water Breathing", "Breathe underwater"),
    RESISTANCE("Resistance", "Take less damage"),
    HASTE("Haste", "Faster digging"),
    REGENERATION("Regeneration", "Regenerate health"),
    NIGHT_VISION("Night Vision", "See in the dark"),
    FIRE_RESISTANCE("Fire Resistance", "Immune to fire"),
    LUCK("Luck", "Increased luck"),
    GLOWING("Glowing", "Become glowing"),
    POISON_CLOUD("Poison Cloud", "Emit a poison cloud"),
    SLOWNESS_AREA("Slowness Area", "Slow nearby foes"),
    HYPERION_BEAM("Hyperion Beam", "Fires an explosive beam"),
    TERMINATOR_VOLLEY("Terminator Volley", "Shoots three arrows"),
    VOID_SLASH("Void Slash", "Dash forward with a swipe");

    private final String displayName;
    private final String description;

    Ability(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return ChatColor.GREEN + displayName;
    }

    public String getDescription() {
        return ChatColor.GRAY + description;
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
