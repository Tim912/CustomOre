package com.example.customitemsystem;

import org.bukkit.ChatColor;

public enum ArmorSet {
    HYPERION_SET("Hyperion Set", Ability.HYPERION_BEAM),
    TERMINATOR_SET("Terminator Set", Ability.TERMINATOR_VOLLEY),
    FROZEN_SET("Frozen Scythe Set", Ability.FROZEN_SCYTHE),
    LIGHTBINDER_SET("Lightbinder Set", Ability.LIGHTBINDER),
    SHADOW_SET("Shadow Set", Ability.SHADOW_RUSH),
    WYNN_ASSASSIN("Wynn Assassin", Ability.SHADOW_RUSH),
    WYNN_MAGE("Wynn Mage", Ability.AETHER_SHIELD),
    WYNN_ARCHER("Wynn Archer", Ability.TERMINATOR_VOLLEY),
    WYNN_WARRIOR("Wynn Warrior", Ability.VOID_SLASH),
    WYNN_SHAMAN("Wynn Shaman", Ability.FROZEN_SCYTHE);

    private final String displayName;
    private final Ability ability;

    ArmorSet(String displayName, Ability ability) {
        this.displayName = displayName;
        this.ability = ability;
    }

    public String getDisplayName() {
        return ChatColor.AQUA + displayName;
    }

    public Ability getAbility() {
        return ability;
    }
}
