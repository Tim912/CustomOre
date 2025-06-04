package com.example.customitemsystem.slayer;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Represents an active slayer quest for a player.
 */
public class SlayerQuest {
    private final Player player;
    private final BossType bossType;
    private final int tier;
    private final int killsNeeded;
    private final EntityType killType;
    private int kills;
    private BossBar bar;

    public SlayerQuest(Player player, BossType bossType, int tier, int killsNeeded, EntityType killType, BossBar bar) {
        this.player = player;
        this.bossType = bossType;
        this.tier = tier;
        this.killsNeeded = killsNeeded;
        this.killType = killType;
        this.kills = 0;
        this.bar = bar;
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

    public EntityType getKillType() {
        return killType;
    }

    public BossBar getBar() {
        return bar;
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
