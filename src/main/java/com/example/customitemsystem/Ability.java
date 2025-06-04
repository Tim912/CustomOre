package com.example.customitemsystem;

import org.bukkit.ChatColor;

public enum Ability {
    FIREBALL("Fireball", "Launches a small fireball."),
    HEAL("Heal", "Restores some of your health."),
    SPEED_BOOST("Speed Boost", "Grants temporary speed."),
    INVISIBILITY("Invisibility", "Makes you invisible briefly."),
    STRENGTH("Strength", "Increases melee damage."),
    JUMP("Jump", "Allows a higher jump."),
    LIGHTNING("Lightning", "Strikes lightning ahead."),
    TELEPORT("Teleport", "Teleports you forward."),
    TRIPLE_SHOT("Triple Shot", "Shoots three arrows without consuming any."),
    ARROW_RAIN("Arrow Rain", "Summons a volley of arrows."),
    HYPERION_STRIKE("Hyperion Strike", "Teleport and explode on arrival."),
    SHADOW_STEP("Shadow Step", "Blink behind your target."),
    BLIZZARD("Blizzard", "Summon a freezing storm."),
    FORTUNE_MINER("Fortune Miner", "Temporarily doubles ore drops."),
    EARTHQUAKE("Earthquake", "Shake the ground to damage mobs."),
    WITHER_SKULL("Wither Skull", "Launches a wither skull."),
    BLINK("Blink", "Quickly teleport a short distance."),
    SONIC_BOOM("Sonic Boom", "Push enemies back with sound."),
    MANA_SHIELD("Mana Shield", "Grants extra absorption hearts."),
    BERSERK("Berserk", "Boost damage and speed at a cost.");

    private final String displayName;
    private final String description;

    Ability(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return ChatColor.AQUA + ChatColor.BOLD.toString() + displayName;
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
