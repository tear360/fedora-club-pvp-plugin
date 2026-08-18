package fr.duelplugin.models;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelPlayer {

    private final UUID uuid;
    private final Map<String, Integer> kills;
    private final Map<String, Integer> deaths;
    private int totalKills;
    private int totalDeaths;
    private int winStreak;
    private int bestWinStreak;

    public DuelPlayer(UUID uuid) {
        this.uuid = uuid;
        this.kills = new HashMap<>();
        this.deaths = new HashMap<>();
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.winStreak = 0;
        this.bestWinStreak = 0;
    }

    public UUID getUuid() { return uuid; }

    public Player getPlayer() {
        return org.bukkit.Bukkit.getPlayer(uuid);
    }

    public int getKills(String mode) {
        return kills.getOrDefault(mode, 0);
    }

    public void addKill(String mode) {
        kills.merge(mode, 1, Integer::sum);
        totalKills++;
    }

    public int getDeaths(String mode) {
        return deaths.getOrDefault(mode, 0);
    }

    public void addDeath(String mode) {
        deaths.merge(mode, 1, Integer::sum);
        totalDeaths++;
    }

    public int getTotalKills() { return totalKills; }
    public int getTotalDeaths() { return totalDeaths; }

    public double getKDR() {
        return totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
    }

    public double getKDR(String mode) {
        int d = getDeaths(mode);
        return d == 0 ? getKills(mode) : (double) getKills(mode) / d;
    }

    public int getWinStreak() { return winStreak; }
    public int getBestWinStreak() { return bestWinStreak; }

    public void addWin() {
        winStreak++;
        if (winStreak > bestWinStreak) {
            bestWinStreak = winStreak;
        }
    }

    public void resetWinStreak() {
        winStreak = 0;
    }

    public int getWins() {
        return kills.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getLosses() {
        return deaths.values().stream().mapToInt(Integer::intValue).sum();
    }
}
