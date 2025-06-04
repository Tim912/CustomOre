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
    EXPLOSION("Explosion", "Creates a small blast."),
    FLIGHT("Flight", "Allows creative flight."),
    WATER_BREATHING("Water Breathing", "Lets you breathe underwater."),
    RESISTANCE("Resistance", "Reduces incoming damage."),
    HASTE("Haste", "Speeds up digging."),
    REGENERATION("Regeneration", "Gradually heals you."),
    NIGHT_VISION("Night Vision", "See clearly in the dark."),
    FIRE_RESISTANCE("Fire Resistance", "Immunity to fire."),
    LUCK("Luck", "Improves loot chances."),
    GLOWING("Glowing", "Makes you glow."),
    POISON_CLOUD("Poison Cloud", "Spreads a poisonous mist."),
    SLOWNESS_AREA("Slowness Area", "Slows nearby targets."),
    /* Additional custom abilities */
    TRIPLE_SHOT("Triple Shot", "Shoots three arrows without consuming any."),
    ARROW_RAIN("Arrow Rain", "Summons a volley of arrows."),
    HYPERION_STRIKE("Hyperion Strike", "Teleport and explode on arrival."),
    VAMPIRIC_TOUCH("Vampiric Touch", "Steal health from foes."),
    BLIZZARD("Blizzard", "Summon a freezing storm."),
    FORTUNE_MINER("Fortune Miner", "Temporarily doubles ore drops."),
    SHADOW_STEP("Shadow Step", "Blink behind your target."),
    EARTHQUAKE("Earthquake", "Shake the ground to damage mobs."),
    WITHER_SKULL("Wither Skull", "Launches a wither skull."),
    CHAIN_LIGHTNING("Chain Lightning", "Zap nearby enemies."),
    BLINK("Blink", "Quickly teleport a short distance."),
    SONIC_BOOM("Sonic Boom", "Push enemies back with sound."),
    MANA_SHIELD("Mana Shield", "Grants extra absorption hearts."),
    THORN_ARMOR("Thorn Armor", "Reflects damage to attackers."),
    ECHO_SLASH("Echo Slash", "Performs a second phantom strike."),
    RADIANT_HEAL("Radiant Heal", "Heals nearby allies."),
    DRAGON_BREATH("Dragon Breath", "Spew lingering dragon flames."),
    CHAOS_ORB("Chaos Orb", "Triggers a random effect."),
    SPIRIT_LINK("Spirit Link", "Share damage with allies."),
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
