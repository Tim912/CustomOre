package com.example.customitemsystem;

import org.bukkit.ChatColor;

public enum Ability {
    FIREBALL("Fireball"),
    HEAL("Heal"),
    SPEED_BOOST("Speed Boost"),
    INVISIBILITY("Invisibility"),
    STRENGTH("Strength"),
    JUMP("Jump"),
    LIGHTNING("Lightning"),
    TELEPORT("Teleport"),
    EXPLOSION("Explosion"),
    FLIGHT("Flight"),
    WATER_BREATHING("Water Breathing"),
    RESISTANCE("Resistance"),
    HASTE("Haste"),
    REGENERATION("Regeneration"),
    NIGHT_VISION("Night Vision"),
    FIRE_RESISTANCE("Fire Resistance"),
    LUCK("Luck"),
    GLOWING("Glowing"),
    POISON_CLOUD("Poison Cloud"),
    SLOWNESS_AREA("Slowness Area");

    private final String displayName;

    Ability(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return ChatColor.GREEN + displayName;
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
