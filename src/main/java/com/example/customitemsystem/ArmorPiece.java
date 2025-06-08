package com.example.customitemsystem;

import org.bukkit.Material;

/** Armor piece types for set creation. */
public enum ArmorPiece {
    HELMET("Helmet", Material.DIAMOND_HELMET),
    CHESTPLATE("Chestplate", Material.DIAMOND_CHESTPLATE),
    LEGGINGS("Leggings", Material.DIAMOND_LEGGINGS),
    BOOTS("Boots", Material.DIAMOND_BOOTS);

    private final String display;
    private final Material material;

    ArmorPiece(String display, Material material) {
        this.display = display;
        this.material = material;
    }

    public String getName() { return display; }

    public Material getMaterial() {
        return material;
    }
}
