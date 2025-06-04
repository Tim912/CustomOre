package com.example.customitemsystem.stats;

/** Simple container for player stats. */
public class PlayerStats {
    public int strength;
    public int dexterity;
    public int defense;
    public int maxMana;
    public int manaRegen;
    public int abilityDamage;
    public int agility;

    public void add(PlayerStats other) {
        if (other == null) return;
        strength += other.strength;
        dexterity += other.dexterity;
        defense += other.defense;
        maxMana += other.maxMana;
        manaRegen += other.manaRegen;
        abilityDamage += other.abilityDamage;
        agility += other.agility;
    }

    public static PlayerStats of(int value) {
        PlayerStats stats = new PlayerStats();
        stats.strength = value;
        stats.dexterity = value;
        stats.defense = value;
        stats.maxMana = value;
        stats.manaRegen = value;
        stats.abilityDamage = value;
        stats.agility = value;
        return stats;
    }
}
