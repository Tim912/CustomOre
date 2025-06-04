package com.example.customitemsystem.morph;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Enum representing the pieces of the Morph set with level requirements.
 */
public enum MorphItem {
    MORPH_STARDUST("Morph-Stardust", Material.DIAMOND_HELMET, 100),
    MORPH_STEEL("Morph-Steel", Material.DIAMOND_CHESTPLATE, 75),
    MORPH_IRON("Morph-Iron", Material.DIAMOND_LEGGINGS, 50),
    MORPH_GOLD("Morph-Gold", Material.DIAMOND_BOOTS, 30),
    MORPH_TOPAZ("Morph-Topaz", Material.GOLD_NUGGET, 20),
    MORPH_EMERALD("Morph-Emerald", Material.EMERALD, 20),
    MORPH_AMETHYST("Morph-Amethyst", Material.AMETHYST_SHARD, 20),
    MORPH_RUBY("Morph-Ruby", Material.REDSTONE, 20);

    private final String displayName;
    private final Material material;
    private final int levelReq;

    MorphItem(String displayName, Material material, int levelReq) {
        this.displayName = displayName;
        this.material = material;
        this.levelReq = levelReq;
    }

    public String getDisplayName() {
        return ChatColor.LIGHT_PURPLE + displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getLevelReq() {
        return levelReq;
    }

    /**
     * Returns the Morph item for the given material or null if none matches.
     */
    public static MorphItem fromMaterial(Material material) {
        for (MorphItem item : values()) {
            if (item.material == material) {
                return item;
            }
        }
        return null;
    }
}
