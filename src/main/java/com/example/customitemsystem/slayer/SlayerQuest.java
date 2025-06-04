package com.example.customitemsystem.slayer;

import org.bukkit.entity.Player;

/**
 * Represents an active slayer quest for a player.
 */
public class SlayerQuest {
    private final Player player;
    private final BossType bossType;
    private final int tier;
    private final int killsNeeded;
    private int kills;

    public SlayerQuest(Player player, BossType bossType, int tier, int killsNeeded) {
        this.player = player;
        this.bossType = bossType;
        this.tier = tier;
        this.killsNeeded = killsNeeded;
        this.kills = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public BossType getBossType() {
        return bossType;
    }

    public int getTier() {
        return tier;
    }

    public int getKillsNeeded() {
        return killsNeeded;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public boolean isComplete() {
        return kills >= killsNeeded;
    }
}
