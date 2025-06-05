package com.example.customitemsystem;

import org.bukkit.ChatColor;

/**
 * Simple rarity classification for custom items.
 */
public enum Rarity {
    COMMON(ChatColor.WHITE),
    UNCOMMON(ChatColor.GREEN),
    RARE(ChatColor.BLUE),
    EPIC(ChatColor.DARK_PURPLE),
    LEGENDARY(ChatColor.GOLD),
    MYTHICAL(ChatColor.LIGHT_PURPLE);

    private final ChatColor color;

    Rarity(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public String format(String name) {
        return color + name;
    }
}
